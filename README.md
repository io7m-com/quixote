quixote
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.quixote/com.io7m.quixote.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.quixote%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.quixote/com.io7m.quixote?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/quixote/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m/quixote.svg?style=flat-square)](https://codecov.io/gh/io7m/quixote)

![com.io7m.quixote](./src/site/resources/quixote.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m/quixote/main.linux.temurin.current.yml)](https://github.com/io7m/quixote/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m/quixote/main.linux.temurin.lts.yml)](https://github.com/io7m/quixote/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m/quixote/main.windows.temurin.current.yml)](https://github.com/io7m/quixote/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m/quixote/main.windows.temurin.lts.yml)](https://github.com/io7m/quixote/actions?query=workflow%3Amain.windows.temurin.lts)|

## quixote

A tiny embedded HTTP server for unit testing.

### Features

  * An embedded HTTP server for simulating external services.
  * Conveniently enqueue responses to arbitrary requests.
  * Verify that requests were received as expected.
  * Zero dependencies.
  * Written in pure Java 17.
  * [OSGi](https://www.osgi.org/) ready.
  * [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System) ready.
  * ISC license.
  * High-coverage automated test suite.

### Motivation

Any code that makes requests to an external service should be tested to
ensure that both the requests it makes are correct, and that it behaves
correctly when presented with various responses. The `quixote` package
provides a tiny embedded web server that can be configured to return canned
responses to specified requests.

### Building

```
$ mvn clean verify
```

### Usage

Create a `QWebServer` before each test. The server will listen on the
specified port.

```
@BeforeEach
public void setup()
  throws IOException
{
  this.server =
    QWebServers.createServer(42000);
  this.http =
    HttpClient.newHttpClient();
}
```

Enqueue responses to requests:

```
this.server.addResponse()
  .forMethod("GET")
  .forPath("/xyz")
  .withContentType("text/plain")
  .withFixedText("Hello 0.")
  .withStatus(201)
  .withHeader("Header-0", "XYZ");
```

Have code make requests to the server during tests, and then verify that
the server received the requests:

```
final var requests =
  new LinkedList<>(this.server.requestsReceived());

{
  final var req = requests.remove(0);
  assertEquals("GET", req.method());
  assertEquals("/xyz", req.path());
}
```

Remember to clean up the server after each test:

```
@AfterEach
public void tearDown()
  throws IOException
{
  this.server.close();
}
```

