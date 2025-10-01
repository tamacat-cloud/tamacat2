FROM eclipse-temurin:25-jdk-alpine

RUN apk update && apk upgrade && apk add --no-cache \
    bash \
    curl

RUN mkdir -p /usr/local/tamacat2/lib
WORKDIR /usr/local/tamacat2

COPY lib/tamacat2-httpd-2.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/tamacat2/lib/

ENV CLASSPATH=/usr/local/tamacat2/lib/tamacat2-httpd-2.0-SNAPSHOT-jar-with-dependencies.jar

ENV BIND_PORT=8080
EXPOSE ${BIND_PORT}

COPY TamacatWebServer.java /usr/local/tamacat2/
COPY htdocs /usr/local/tamacat2/htdocs

CMD java -cp ${CLASSPATH} TamacatWebServer.java

#ENTRYPOINT ["/bin/sh", "-c", "while :; do sleep 10; done"]
