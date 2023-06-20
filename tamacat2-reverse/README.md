# tamacat2-reverse : Reverse Proxy
- The tamacat2-reverse is an open source reverse proxy server software.

## Features:
- Customizable Reverse Proxy.
- Dependencies: Apache HttpComponents httpcore5, SLF4J+Logback
- Required Java 17+ (JRE/JDK)

## Getting Started:

### Examples:
- ReverseProxyExamples.java
  ```java
  import cloud.tamacat2.httpd.config.HttpConfig;
  import cloud.tamacat2.reverse.ReverseProxy;
  import cloud.tamacat2.reverse.config.ReverseUrlConfig;
  import cloud.tamacat2.reverse.config.ReverseConfig;

  public class ReverseProxyExamples {
    public static void main(String[] args) {
      new ReverseProxy().startup(HttpConfig.create().port(80)
        .urlConfig(ReverseUrlConfig.create().path("/examples/")
          .reverse(ReverseConfig.create().url("http://localhost:8080/examples/"))
        )
      );
    }
  }
  ```

### Execute:
- ex. Java17+ / ReverseProxyExamples.java
  ```
  java -cp tamacat2-reverse-2.0-SNAPSHOT-jar-with-dependencies.jar ReverseProxyExamples.java
  ```
  - Browser access to http://localhost/examples/

## License:
- The tamacat2-reverse is licensed under the terms of the [Apache License, Version 2.0](https://github.com/tamacat-cloud/tamacat2/blob/main/LICENSE.txt).

- Bundle Licensing:
  - [Apache HttpComponents HttpCore / Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
  - [Licensing terms for SLF4J](http://www.slf4j.org/license.html)
  - [Logback License](https://logback.qos.ch/license.html)
  - [Jetty Licensing | The Eclipse Foundation](https://www.eclipse.org/jetty/licenses.php)
