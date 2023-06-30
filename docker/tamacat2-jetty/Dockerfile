FROM maven:3.8.5-eclipse-temurin-17-alpine as BUILD
RUN apk update && apk upgrade && apk add --no-cache \
    bash \
    curl \
    git
    
WORKDIR /build
RUN git clone https://github.com/tamacat-cloud/tamacat2.git
WORKDIR /build/tamacat2
RUN --mount=type=cache,target=/root/.m2 mvn clean package

###################################################3
FROM eclipse-temurin:17-jdk-alpine

RUN apk update && apk upgrade && apk add --no-cache \
    bash \
    curl

RUN mkdir -p /usr/local/tamacat2/lib
WORKDIR /usr/local/tamacat2

COPY --from=BUILD /build/tamacat2/tamacat2-jetty/target/tamacat2-jetty-2.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/tamacat2/lib/

ENV CLASSPATH=/usr/local/tamacat2/lib/tamacat2-jetty-2.0-SNAPSHOT-jar-with-dependencies.jar

ENV BIND_PORT=8080
EXPOSE ${BIND_PORT}

COPY JettyExamples.java /usr/local/tamacat2/
COPY webapps /usr/local/tamacat2/webapps

CMD java -cp /usr/local/tamacat2/lib/tamacat2-jetty-2.0-SNAPSHOT-jar-with-dependencies.jar JettyExamples.java

#ENTRYPOINT ["/bin/sh", "-c", "while :; do sleep 10; done"]
