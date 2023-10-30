FROM maven:3.9.5-eclipse-temurin-21-alpine as BUILD
RUN apk update && apk upgrade && apk add --no-cache \
    bash \
    curl \
    git
    
WORKDIR /build
RUN git clone https://github.com/tamacat-cloud/tamacat2.git
WORKDIR /build/tamacat2
RUN --mount=type=cache,target=/root/.m2 mvn clean package

###################################################3
FROM eclipse-temurin:21-jdk-alpine

RUN apk update && apk upgrade && apk add --no-cache \
    bash \
    curl

RUN mkdir -p /usr/local/tamacat2/lib
WORKDIR /usr/local/tamacat2

COPY --from=BUILD /build/tamacat2/tamacat2-reverse/target/tamacat2-reverse-2.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/tamacat2/lib/

ENV CLASSPATH=/usr/local/tamacat2/lib/tamacat2-reverse-2.0-SNAPSHOT-jar-with-dependencies.jar

ENV BIND_PORT=8080
EXPOSE ${BIND_PORT}

COPY ReverseProxyExamples.java /usr/local/tamacat2/
CMD java -cp /usr/local/tamacat2/lib/tamacat2-reverse-2.0-SNAPSHOT-jar-with-dependencies.jar ReverseProxyExamples.java

#ENTRYPOINT ["/bin/sh", "-c", "while :; do sleep 10; done"]
