# tamacat2-jetty

- The tamacat2-jetty is an open source Java Web Server software, powered by "Apache HttpComponents-5.2".
- This is a customizable HTTP/HTTPS Server / Reverse Proxy with Embedded Jetty.
- Base image: eclipse-temurin:21-jdk-alpine (OpenJDK 21)

## GitHub
https://github.com/tamacat-cloud/tamacat2/


## DIRECTORY
- /usr/local/tamacat2/lib
- /usr/local/tamacat2/htdocs
- /usr/local/tamacat2/webapps


## ENVIRONMENT
- BIND_PORT=8080

# docker-compose
```
docker-compose up -d --build
```

## Examples Web Application
* http://localhost:8080/examples/
