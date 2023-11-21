### https://hub.docker.com/_/maven/tags
FROM maven:3-eclipse-temurin-21-alpine as BUILD
RUN apk update && apk upgrade && apk add --no-cache \
    bash \
    curl \
    git

WORKDIR /build
RUN git clone https://github.com/tamacat-cloud/tamacat2.git
WORKDIR /build/tamacat2
RUN --mount=type=cache,target=/root/.m2 mvn clean package


### https://www.graalvm.org/latest/docs/getting-started/container-images/
FROM ghcr.io/graalvm/native-image-community:21 as NATIVE
RUN mkdir -p /work
COPY --from=BUILD /build/tamacat2/tamacat2-reverse/target/tamacat2-reverse-2.0-SNAPSHOT-jar-with-dependencies.jar /work

WORKDIR /work
COPY ReverseProxyExamples.java /work
RUN javac -cp /work/tamacat2-reverse-2.0-SNAPSHOT-jar-with-dependencies.jar ReverseProxyExamples.java

RUN native-image --enable-url-protocols=http --no-fallback -cp /work -cp /work/tamacat2-reverse-2.0-SNAPSHOT-jar-with-dependencies.jar ReverseProxyExamples tamacat2-reverse-examples
#RUN native-image --enable-url-protocols=http --no-fallback -H:ReflectionConfigurationFiles=/work/reflection-config.json -cp /work -cp /work/tamacat2-reverse-2.0-SNAPSHOT-jar-with-dependencies.jar JettyExamples tamacat2-reverse-examples

### https://github.com/GoogleContainerTools/distroless/issues/217
# error while loading shared libraries: libz.so.1: cannot open shared object file: No such file or directory
#FROM debian:stable-slim AS build-env
FROM ubuntu
#RUN apt update && apt upgrade

###
#FROM gcr.io/distroless/cc-debian11:debug
#FROM gcr.io/distroless/cc-debian10
#FROM gcr.io/distroless/static-debian11:debug
#FROM gcr.io/distroless/base

#COPY --from=build-env /lib/x86_64-linux-gnu/libz.so* /lib/x86_64-linux-gnu/
#COPY --from=build-env /lib/x86_64-linux-gnu/libc.so* /lib/x86_64-linux-gnu/
COPY --from=NATIVE /work/tamacat2-reverse-examples /home/tamacat2/tamacat2-reverse-examples

WORKDIR /home/tamacat2
USER 1001
EXPOSE 8080

ENTRYPOINT ["/home/tamacat2/tamacat2-reverse-examples", "/examples/", "http://host.docker.internal:8088/examples/"]
#ENTRYPOINT ["/bin/sh", "-c", "while :; do sleep 10; done"]
