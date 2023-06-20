# tamacat2 - Java Reverse Proxy
- The tamacat2 is an open source reverse proxy server software.

## Basic Concepts:
- Lightweight, Low dependency and Less Configuration.

## Features:
- Customizable Reverse Proxy.
- Dependencies: Apache HttpComponents httpcore5, SLF4J+Logback
- Support Embedded Jetty11 Servlet/JSP (optional)
- Required Java 17+ (JRE/JDK)

## Getting Started:

### Examples: ReverseProxy with Jetty
- Create JettyExamples.java
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

- mvn package
- copy target/tamacat2-jetty-2.0-SNAPSHOT-jar-with-dependencies.jar to current directory.

* Compile JettyExamples.java
  ```
  javac -cp tamacat2-jetty-2.0-SNAPSHOT-jar-with-dependencies.jar JettyExamples.java
  ```

## License:
The tamacat2 is licensed under the terms of the Apache License, Version 2.0.
