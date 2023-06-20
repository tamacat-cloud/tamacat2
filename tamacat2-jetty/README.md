# tamacat2-jetty : Reverse Proxy with Jetty
- The tamacat2-jetty is an open source reverse proxy with embedded Jetty server.

## Features:
- Support Embedded Jetty11 Servlet/JSP
  - https://www.eclipse.org/jetty/
- Required Java 17+ (JRE/JDK)

## Getting Started:

### Examples:
- JettyExamples.java
  ```java
  import cloud.tamacat2.httpd.config.HttpConfig;
  import cloud.tamacat2.jetty.config.JettyUrlConfig;
  import cloud.tamacat2.jetty.JettyServer;
  import cloud.tamacat2.reverse.config.ReverseConfig;

  public class JettyExamples {
    public static void main(String[] args) {
      new JettyServer().startup(HttpConfig.create().port(80)
        .urlConfig(JettyUrlConfig.create()
          .path("/examples/")
          .reverse(ReverseConfig.create()
            .url("http://127.0.0.1:8080/examples/")
          )
        )
      );
    }
  }
  ```

- Create webapps/examples/index.jsp
  ```
  <html>
  <body>
  examples
  </body>
  </html>
  ```

### Execute
- ex. Java17+ / JettyExamples.java
  ```
  java -cp tamacat2-jetty-2.0-SNAPSHOT-jar-with-dependencies.jar JettyExamples.java
  ```
  - Browser access to http://localhost/examples/

## License:
- The tamacat2-jetty is licensed under the terms of the [Apache License, Version 2.0](https://github.com/tamacat-cloud/tamacat2/blob/main/LICENSE.txt).

- Bundle Licensing:
  - [Apache HttpComponents HttpCore / Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
  - [Licensing terms for SLF4J](http://www.slf4j.org/license.html)
  - [Logback License](https://logback.qos.ch/license.html)
  - [Jetty Licensing | The Eclipse Foundation](https://www.eclipse.org/jetty/licenses.php)
