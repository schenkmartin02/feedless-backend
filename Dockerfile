# Ez az image NEM forditja a jart - azt a CI mar megtette a `./mvnw verify`-jal,
# itt csak bemasoljuk a kesz artefaktot. Igy az image epiteseben nincs
# architektura-fuggo lepes, tehat x86-os GitHub runneren is masodpercek alatt
# keszul arm64-es (Hetzner CAX) image, emulacio nelkul.
#
# Elofeltetel: `./mvnw package` (vagy `verify`) mar lefutott, es letezik a target/*.jar.

FROM eclipse-temurin:21-jre

WORKDIR /app

# Nem root felhasznalo. Ha az alkalmazasban valaha talalnak egy tavoli
# kodfuttatasi hibat, a tamado ne rootkent alljon a konteneren belul.
RUN useradd --system --create-home --uid 10001 feedless

ARG JAR_FILE=target/*.jar
COPY --chown=feedless:feedless ${JAR_FILE} /app/app.jar

USER feedless

# A JVM konteneren belul alapbol a memoria-limit 25%-at veszi heapnek.
# Ebben a konteneren rajta kivul nincs mas, ami memoriat hasznalna, ezert
# 75% a helyes ertek - a maradek a metaspace-nek, a szalveremnek es a
# direct bufferek­nek kell.
#
# Az ExitOnOutOfMemoryError azert kell, hogy a JVM memoriahiany eseten
# AZONNAL meghaljon, es a compose restart policy-je ujrainditsa. Enelkul
# a folyamat orakig vergodne 100% CPU-n, GC-zve, de kereseket kiszolgalni
# kepteleul - az a legrosszabb allapot, amit egy monitor nehezen vesz eszre.
#
# A JAVA_TOOL_OPTIONS-t a JVM automatikusan beolvassa, tehat a compose-bol
# felul lehet irni anelkul, hogy az image-hez hozza kellene nyulni.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Csak dokumentacio: nem publikal portot, csak jelzi, mit hallgat az app.
# A portot a compose teszi elerhetove a belso halozaton.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
