# tamacat2-httpd : Web Server
- The tamacat2-httpd is an open source Web server software.

## Features:
- Standard HTTP/HTTPS Web Server.
- Dependencies: Apache HttpComponents httpcore5, SLF4J+Logback
- Required Java 21+ (JRE/JDK)

## Getting Started:

### Examples:
- WebServerExamples.java
  ```java
  import cloud.tamacat2.httpd.WebServer;
  import cloud.tamacat2.httpd.config.HttpConfig;
  import cloud.tamacat2.httpd.config.UrlConfig;
  
  public class WebServerExamples {
    public static void main(String[] args) {
      new WebServer().startup(HttpConfig.create().port(8080)
        .urlConfig(UrlConfig.create().path("/examples/")
          .docsRoot("${server.home}/htdocs/")
        )
        .contentEncoding("gzip")
      );
    }
  }
  ```
  - docsRoot(String path) ... Static Web Contents directory. (HTML/CSS/Images etc.)
    - ${server.home} ... Variable for server home path.
  - contentEncoding("gzip") ... Compress response body (gzip)


### Execute:
- ex. Java21+ / WebServerExamples.java
  ```
  java -cp tamacat2-httpd-2.0-SNAPSHOT-jar-with-dependencies.jar WebServerExamples.java
  ```

  - Browser access to http://localhost:8080/examples/

## License:
- The tamacat2-httpd is licensed under the terms of the [Apache License, Version 2.0](https://github.com/tamacat-cloud/tamacat2/blob/main/LICENSE.txt).

- Bundle Licensing:
  - [Apache HttpComponents HttpCore / Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
  - [Licensing terms for SLF4J](http://www.slf4j.org/license.html)
  - [Logback License](https://logback.qos.ch/license.html)
