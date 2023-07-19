# tamacat2-httpd

- The tamacat2-httpd is an open source Java Web Server software, powered by "Apache HttpComponents-5.2".
- This is a customizable HTTP/HTTPS Server framework.
- Base image: eclipse-temurin:17-jdk-alpine (OpenJDK 17)

## GitHub
https://github.com/tamacat-cloud/tamacat2/


## DIRECTORY
- /usr/local/tamacat-httpd/lib
- /usr/local/tamacat-httpd/htdocs

## ENVIRONMENT
- BIND_PORT=8080

# docker-compose
```
docker-compose up -d --build
```

## Examples Web Application
* http://localhost:8080/examples/
