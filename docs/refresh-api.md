# Refresh gomb — API szerződés

Ez a dokumentum a profil oldali „frissítés" gomb backend szerződését írja le a frontend számára.

## A végpont

```
POST /players/{region}/{name}/{tag}/refresh
```

| Rész | Érték |
|---|---|
| `region` | `EUNE` vagy `EUW` (kisbetűs is jó: `eune`) |
| `name` | Riot ID név rész, URL-enkódolva |
| `tag` | Riot ID tag, `#` nélkül |
| Body | nincs |
| Query paraméter | nincs |

Ugyanaz a három path paraméter, mint a profil GET-nél, csak `/refresh` a végén.

## Válaszok

| Kód | Jelentés | Body | Fejléc |
|---|---|---|---|
| **202** | Frissítés beütemezve | üres | — |
| **429** | Cooldown aktív, túl gyakran nyomták | üres | `Retry-After: 300` (másodperc) |
| **404** | A backend nem ismeri ezt a játékost | üres | — |
| **400** | Hibás régió az URL-ben | hibaobjektum | — |

**Fontos: a 202 nem azt jelenti, hogy kész, hanem hogy sorba került.** A tényleges crawl
utána fut le, jellemzően pár másodperc alatt, de terhelés alatt akár fél percig is eltarthat.

**A 404-ről:** a profil GET (`GET /players/{region}/{name}/{tag}`) ismeretlen játékosnál
magától felveszi a rendszerbe. Ezért a refresh gomb csak olyan oldalon jelenjen meg, ahol a
GET már lefutott — így 404 gyakorlatilag nem fordulhat elő. Ha mégis, az azt jelenti, hogy a
játékos kiesett a rendszerből; a helyes reakció egy sima oldal-újratöltés.

## A folyamat, amit a frontendnek meg kell csinálnia

1. A felhasználó rányom a frissítés gombra.
2. `POST .../refresh`
3. **202** esetén: gomb letiltása, spinner indítása, és **2 másodpercenként hívd a már meglévő
   profil GET-et** ugyanarra a játékosra.
4. Amikor a GET válaszában a `refreshing` mező `false` lesz → spinner le, adatok kicserélése,
   gomb újra aktív.
5. **Biztonsági időkorlát: 60 másodperc.** Ha addig nem lett `false`, állítsd le a pollozást, és
   mutass hibaüzenetet („A frissítés nem sikerült, próbáld újra"). Erre azért van szükség, mert
   egy crawl el is hasalhat — akkor a `refreshing` magától `false` lesz ugyan, de ha a Riot API
   nem válaszol, a job beragadhat, és nem akarunk örökké pörgő spinnert.
6. **429** esetén: ne indíts spinnert. Mutass egy rövid üzenetet, hogy nemrég frissült, és
   próbálja később. A `Retry-After` fejléc másodpercben mondja meg, mennyi múlva van értelme
   újra nyomni — ebből visszaszámláló is építhető, de tudni kell, hogy ez **felső becslés**, nem
   pontos hátralévő idő.

## A cooldown

Jelenleg **5 perc** (`crawler.refresh.cooldown-minutes`). Egy játékost 5 percen belül nem lehet
kétszer frissíteni — a második kattintás 429-et kap. Ez szerver oldali szabály, a frontend nem
tudja megkerülni, de érdemes a gombot a 202 után is letiltva hagyni pár másodpercig, hogy ne
generáljon felesleges 429-eket.

Kivétel: ha a játékost **még soha nem crawloltuk**, a cooldown nem érvényes — ilyenkor mindig
202 jön.

## A `refreshing` mező

A profil GET (`GET /players/{region}/{name}/{tag}`) válasza kapott egy új mezőt:

```
refreshing: boolean
```

`true`, amíg a frissítés folyamatban van — vagyis a játékos sorban áll vagy épp crawlolás alatt
van —, `false`, ha nincs folyamatban semmi. Soha nem `null`.

Ez az egyetlen jel, amiből a frontend megtudja, hogy a spinner mehet-e le. Ne az
`updatedMinutesAgo`-ból következtess rá: ha a játékosnak nem volt új meccse, az az érték a
frissítés után is változatlan maradhat.

Amire figyelni kell:

- A `refreshing` **csak a felhasználó által kért** frissítéseket jelzi. A háttérben futó
  automatikus újra-crawl nem billenti `true`-ra, tehát nem fog random profilokon spinner
  megjelenni.
- Ha a crawl hibára fut, a `refreshing` `false` lesz, de az adat változatlan marad. Ezt a
  frontend nem tudja megkülönböztetni a sikeres, de üres eredménytől — ezért kell a 60
  másodperces időkorlát is.

## Eddig ismeretlen játékos keresése

Ha olyan játékos profiljára navigálnak, akit a backend még nem ismer, a **profil GET** akkor is
**200**-zal válaszol, de az adatok üresek (`profileIconId: 0`, `level: 0`, a rangok `null`, a
`form` üres tömb) — és a `refreshing` mező `true`.

Ez nem hibaállapot: a backend ilyenkor felveszi a játékost és azonnal sorba teszi. A frontend
teendője pontosan ugyanaz, mint a refresh gomb után: spinner, 2 másodpercenkénti pollozás,
és amikor a `refreshing` `false` lesz, a kirajzolt adat cseréje. Külön kezelést nem igényel,
ugyanaz a kódág kiszolgálja.

A GET **404**-et csak akkor ad, ha a Riot API sem ismeri a nevet — vagyis tényleg nincs ilyen
játékos.