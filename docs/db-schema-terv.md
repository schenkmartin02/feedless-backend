# Adatbázis séma terv — 2. fázis (perzisztencia)

Papíros terv a `V1`–`V3` Flyway migrációkhoz. A döntések (surrogate key, UNIQUE a
Riot-azonosítókon, stub sor, composite védelem, explicit player-index) a tervezési
beszélgetésben születtek — itt csak rögzítve vannak, indoklással.

## Alapdöntések

- **Surrogate PK mindenhol** (`BIGINT GENERATED ALWAYS AS IDENTITY`): a puuid és a
  match_id a Riot tulajdona, bármikor változhat. A saját táblák egymás `id`-jára
  hivatkoznak, így egy Riot-oldali azonosítócsere egyetlen oszlop frissítése.
  Plusz: 8 byte-os FK a 78/32 karakteres stringek helyett a milliós `participants` táblában.
- **UNIQUE a Riot-azonosítókon** (`players.puuid`, `matches.match_id`): a surrogate
  séma önmagában nem véd a duplikátumok ellen — ez igen. Ingyen indexet is ad.
- **Stub sor minta**: ismeretlen puuid-hoz azonnal létrejön a `players` sor (id + puuid),
  a többi oszlop nullable. A crawler tölti ki később; ha a felhasználó előbb rákeres,
  a queue-ban előre sorolható (3. fázis).

## `players`

| Oszlop | Típus | Megkötés | Miért |
|---|---|---|---|
| `id` | `BIGINT GENERATED ALWAYS AS IDENTITY` | PK | Surrogate — a saját táblák erre hivatkoznak, a Riot nem tud belenyúlni. |
| `puuid` | `VARCHAR(78)` | NOT NULL, UNIQUE | A puuid pontosan 78 karakter. Duplikátumvédelem + ingyen index a puuid-alapú kereséshez. |
| `game_name` | `VARCHAR(16)` | NULL | Riot ID név-része, max 16 karakter. Stub sorban még nincs meg. |
| `tag_line` | `VARCHAR(5)` | NULL | A `#EUNE`-szerű tag, max 5. Stub miatt nullable. |
| `profile_updated_at` | `TIMESTAMPTZ` | NULL | `NULL` = stub (sosem volt lekérve), különben = utolsó frissítés ideje. Egy oszlop, két információ: stub-e és mennyire friss. A "mi a teendő + prioritás" a `crawl_queue`-ba megy (3. fázis). |

A rank NEM ide kerül: egy játékosnak több rankja van (soloq + flex) — ez 1:N kapcsolat,
külön táblába való (`player_ranks`). Martin észrevétele; az API-oldal is ezt tükrözi
(League-V4 → `Set<LeagueEntryDto>`).

## `player_ranks`

| Oszlop | Típus | Megkötés | Miért |
|---|---|---|---|
| `id` | `BIGINT GENERATED ALWAYS AS IDENTITY` | PK | Surrogate. |
| `player_id` | `BIGINT` | NOT NULL, FK → `players(id)` | Kihez tartozik. |
| `queue_type` | `VARCHAR(20)` | NOT NULL | `RANKED_SOLO_5x5` / `RANKED_FLEX_SR` — a League-V4 értékei változtatás nélkül. TFT szándékosan nincs: külön játék, külön API, scope-on kívül. |
| `tier` | `VARCHAR(12)` | NOT NULL | `CHALLENGER`… |
| `division` | `VARCHAR(4)` | NOT NULL | `I`–`IV`. |
| `league_points` | `INT` | NOT NULL | LP. |
| `wins` | `INT` | NOT NULL | A League-V4 adja; queue-nkénti winrate-hez. |
| `losses` | `INT` | NOT NULL | Ugyanaz. |

- `UNIQUE (player_id, queue_type)` — egy játékosnak queue-nként egy rank-sora van;
  frissítéskor upsert. A vezető oszlop miatt a `player_id` FK-hoz külön index sem kell.
- Minden oszlop NOT NULL lehet, mert a "nincs rank" állapotot a **sor hiánya** fejezi ki,
  nem NULL-ok. (A stub/unranked játékosnak egyszerűen nincs itt sora.)

## `matches`

| Oszlop | Típus | Megkötés | Miért |
|---|---|---|---|
| `id` | `BIGINT GENERATED ALWAYS AS IDENTITY` | PK | Surrogate. |
| `match_id` | `VARCHAR(32)` | NOT NULL, UNIQUE | `EUN1_1234567890` formátum (~16 kar., 32 ráhagyással). A UNIQUE garantálja, hogy egy meccs csak egyszer kerül be. |
| `patch` | `VARCHAR(16)` | NOT NULL | A `gameVersion`-ből kinyert patch (pl. `15.14`). Minden statisztika ezen csoportosít. |
| `queue_id` | `INT` | NOT NULL | 420 = soloq stb. |
| `game_start` | `TIMESTAMPTZ` | NOT NULL | A Riot ms-timestampjéből. A match history `ORDER BY` oszlopa. |
| `game_duration` | `INT` | NOT NULL | Másodpercben. Későbbi statokhoz (pl. CS/perc). |

## `participants`

| Oszlop | Típus | Megkötés | Miért |
|---|---|---|---|
| `id` | `BIGINT GENERATED ALWAYS AS IDENTITY` | PK | Surrogate. |
| `match_id` | `BIGINT` | NOT NULL, FK → `matches(id)` | 8 byte-os hivatkozás a string helyett. |
| `player_id` | `BIGINT` | NOT NULL, FK → `players(id)` | A stub minta miatt lehet NOT NULL: beszúráskor a player sor (akár stubként) már létezik. |
| `champion_id` | `INT` | NOT NULL | Riot numerikus champion id. A nevet NEM tároljuk — statikus adat (Data Dragon), az API-rétegben oldjuk fel. |
| `team_position` | `VARCHAR(8)` | NOT NULL | `TOP`/`JUNGLE`/… — a későbbi role-alapú statokhoz; utólag visszatölteni fájdalmas. |
| `kills` | `INT` | NOT NULL | |
| `deaths` | `INT` | NOT NULL | |
| `assists` | `INT` | NOT NULL | |
| `win` | `BOOLEAN` | NOT NULL | |

### Megkötések és indexek a `participants`-on

- `UNIQUE (match_id, player_id)` — egy játékos egyszer szerepelhet egy meccsben;
  véd a dupla crawler-feldolgozás ellen is.
- `CREATE INDEX ... ON participants (player_id)` — **explicit kell**, mert a Postgres
  FK-ra nem rak automatikusan indexet. Ez gyorsítja a match historyt (`WHERE player_id = ?`).
- Külön `match_id` index NEM kell: a `UNIQUE (match_id, player_id)` index vezető
  oszlopa a `match_id`, a join azt használja. (Ezért ez az oszlopsorrend a UNIQUE-ban.)

## A fő lekérdezés, amire a séma épül (match history)

```sql
SELECT m.match_id, m.game_start, p.champion_id,
       p.kills, p.deaths, p.assists, p.win
FROM participants p
JOIN matches m ON m.id = p.match_id
WHERE p.player_id = ?
ORDER BY m.game_start DESC
LIMIT 20;
```

Szűrés: `participants(player_id)` index. Join: `matches` PK. Rendezés: `matches.game_start`.

## Szándékosan NINCS benne

- `crawl_queue` — 3. fázis, ott tervezzük meg.
- Item/rúna oszlopok — későbbi migrációban (erre való a Flyway).

## Következő lépés (Martin írja)

1. `V1__create_players.sql`
2. `V2__create_matches.sql`
3. `V3__create_participants.sql`
4. `V4__create_player_ranks.sql`

A sorrend kötött: FK csak már létező táblára mutathat.

ssh tunel test:
```
ssh -N -L 2375:127.0.0.1:2375 root@192.168.0.62
```