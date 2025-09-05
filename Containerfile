FROM docker.io/clojure:temurin-21-tools-deps-bookworm AS build

COPY . /app
WORKDIR /app
RUN clojure -T:build uberjar

FROM docker.io/clojure:temurin-21-tools-deps-bookworm AS run

COPY --from=build /app/target/net.clojars.akosiaris/myexpenses2hledger-*-standalone.jar /app/myexpenses2hledger.jar

ENTRYPOINT ["java", "-jar", "/app/myexpenses2hledger.jar"]
