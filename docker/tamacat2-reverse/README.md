# tamacat2-reverse

- The tamacat2-reverse is an open source Java Web Server software, powered by "Apache HttpComponents-5.2".
- This is a customizable HTTP/HTTPS Server / Reverse Proxy.
- Base image: eclipse-temurin:25-jdk-alpine (OpenJDK 25)

## GitHub
https://github.com/tamacat-cloud/tamacat2/

```
FROM tamacat/tamacat2-reverse:latest
```

## docker start
```
docker run --rm -it -d -p 8080:8080 tamacat/tamacat2-reverse:latest 
```

* There is no contents in the initial state. (404 Not Found)


### Container bind port
  * emvironment:
    - BIND_PORT=8080

### Dockerfile
```
FROM tamacat/tamacat2-reverse:latest

```

# docker-compose
```
docker-compose up -d --build
```

## Examples Web Application
* http://localhost:8080/examples/
