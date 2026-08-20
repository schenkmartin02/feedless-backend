#!/usr/bin/env bash
#
# Feedless adatbazis-mentes. Ket uzemmodban fut:
#
#   backup.sh daily   - csak a POTOLHATATLAN tablak (pillanatfelvetelek, ladder).
#                       Ezeket semmibol nem lehet ujraszamolni: egy adott napi
#                       allapotot rogzitenek, ami visszamenoleg nem allithato elo.
#                       Par MB, ezert napi mentes es hosszu megorzes fer bele.
#
#   backup.sh full    - a teljes adatbazis. Nagy, ezert ritkabban es rovidebb
#                       megorzessel. A matches/participants elvben ujra-crawlolhato,
#                       de az napokba telne.
#
# set -e   : elso hibanal all
# set -u   : definialatlan valtozo hiba
# set -o pipefail : egy pipe akkor is hibas, ha nem az utolso tagja bukik el
set -euo pipefail

COMPOSE_DIR=/opt/feedless
BACKUP_DIR=/opt/feedless/backups
KEEP_DAILY_DAYS=14
KEEP_FULL_DAYS=14

MODE="${1:-daily}"
STAMP="$(date +%Y%m%d-%H%M)"

# Ezek a tablak nem szamolhatok ujra semmibol: napi allapotot rogzitenek.
SNAPSHOT_TABLES=(
  champion_rank_snapshot
  champion_ban_snapshot
  ladder_snapshot
  rank_leaderboard
  site_stats
)

mkdir -p "$BACKUP_DIR"
cd "$COMPOSE_DIR"

case "$MODE" in
  daily)
    TARGET="$BACKUP_DIR/snapshots-$STAMP.dump"
    ARGS=()
    for t in "${SNAPSHOT_TABLES[@]}"; do ARGS+=(-t "$t"); done
    docker compose exec -T postgres \
      pg_dump -U feedless -d feedless --data-only -Fc "${ARGS[@]}" > "$TARGET"
    KEEP_DAYS=$KEEP_DAILY_DAYS
    PATTERN="snapshots-*.dump"
    ;;
  full)
    TARGET="$BACKUP_DIR/full-$STAMP.dump"
    docker compose exec -T postgres \
      pg_dump -U feedless -d feedless -Fc > "$TARGET"
    KEEP_DAYS=$KEEP_FULL_DAYS
    PATTERN="full-*.dump"
    ;;
  *)
    echo "Hasznalat: $0 [daily|full]" >&2
    exit 2
    ;;
esac

# Epsegellenorzes. Egy 0 byte-os fajl is "sikeres" mentesnek latszana a
# konyvtarban - pedig pont akkor derulne ki a baj, amikor visszaallitanad.
SIZE=$(stat -c %s "$TARGET")
if [ "$SIZE" -lt 1024 ]; then
  echo "HIBA: a mentes gyanusan kicsi ($SIZE byte): $TARGET" >&2
  exit 1
fi

# Retencio: csak a sajat mintajara illeszkedo, regi fajlokat torli.
find "$BACKUP_DIR" -name "$PATTERN" -type f -mtime +"$KEEP_DAYS" -delete

echo "$(date -Is) $MODE mentes kesz: $TARGET ($(numfmt --to=iec "$SIZE"))"
