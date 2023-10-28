/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.quixote.tests;

import com.io7m.quixote.core.QWebServerType;
import com.io7m.quixote.core.QWebServers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static java.net.http.HttpResponse.BodyHandlers.ofByteArray;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class QWebServersTest
{
  private QWebServerType server;
  private HttpClient http;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.server =
      QWebServers.createServer(42000);
    this.http =
      HttpClient.newHttpClient();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    this.server.close();
  }

  /**
   * The base URI matches.
   */

  @Test
  public void testURL()
  {
    assertEquals(
      URI.create("http://localhost:42000/"),
      this.server.uri()
    );
  }

  /**
   * Test a series of GET requests.
   *
   * @throws Exception On errors
   */

  @Test
  public void testGET_0()
    throws Exception
  {
    /* Arrange */

    this.server.addResponse()
      .forMethod("GET")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 0.")
      .withStatus(201)
      .withHeader("Header-0", "XYZ");

    this.server.addResponse()
      .forMethod("GET")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 1.")
      .withStatus(201)
      .withHeader("Header-1", "ABC");

    assertEquals(2, this.server.responses().size());

    assertEquals(
      List.of(
        "[Response [Method GET] [Path ^/xyz]]",
        "[Response [Method GET] [Path ^/xyz]]"
      ),
      this.server.responses()
        .stream()
        .map(Object::toString)
        .toList()
    );

    /* Act */

    final var request =
      HttpRequest.newBuilder(this.server.uri().resolve("xyz"))
        .build();

    final var response0 =
      this.http.send(request, ofString());

    final var response1 =
      this.http.send(request, ofString());

    final var response2 =
      this.http.send(request, ofString());

    /* Assert */

    final var requests =
      new LinkedList<>(this.server.requestsReceived());

    assertEquals(201, response0.statusCode());
    assertEquals(
      "Hello 0.",
      response0.body()
    );
    assertEquals(
      "text/plain",
      response0.headers()
        .firstValue("Content-Type")
        .orElseThrow()
    );
    assertEquals(
      "XYZ",
      response0.headers()
        .firstValue("Header-0")
        .orElseThrow()
    );

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/xyz", req.path());
    }

    assertEquals(201, response1.statusCode());
    assertEquals(
      "Hello 1.",
      response1.body()
    );
    assertEquals(
      "ABC",
      response1.headers()
        .firstValue("Header-1")
        .orElseThrow()
    );
    assertEquals(
      "text/plain",
      response1.headers()
        .firstValue("Content-Type")
        .orElseThrow()
    );

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/xyz", req.path());
    }

    assertEquals(503, response2.statusCode());
    assertEquals(
      "No matching responses for method GET and path '/xyz'.",
      response2.body()
    );

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/xyz", req.path());
    }

    assertEquals(0, requests.size());
  }

  /**
   * Test a series of GET requests.
   *
   * @throws Exception On errors
   */

  @Test
  public void testGET_1()
    throws Exception
  {
    /* Arrange */

    this.server.close();
    this.server = QWebServers.createServerForAll(42000);

    this.server.addResponse()
      .forMethod("GET")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 0.")
      .withStatus(201)
      .withHeader("Header-0", "XYZ");

    this.server.addResponse()
      .forMethod("GET")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 1.")
      .withStatus(201)
      .withHeader("Header-1", "ABC");

    assertEquals(2, this.server.responses().size());

    assertEquals(
      List.of(
        "[Response [Method GET] [Path ^/xyz]]",
        "[Response [Method GET] [Path ^/xyz]]"
      ),
      this.server.responses()
        .stream()
        .map(Object::toString)
        .toList()
    );

    /* Act */

    final var request =
      HttpRequest.newBuilder(this.server.uri().resolve("xyz"))
        .build();

    final var response0 =
      this.http.send(request, ofString());

    final var response1 =
      this.http.send(request, ofString());

    final var response2 =
      this.http.send(request, ofString());

    /* Assert */

    final var requests =
      new LinkedList<>(this.server.requestsReceived());

    assertEquals(201, response0.statusCode());
    assertEquals(
      "Hello 0.",
      response0.body()
    );
    assertEquals(
      "text/plain",
      response0.headers()
        .firstValue("Content-Type")
        .orElseThrow()
    );
    assertEquals(
      "XYZ",
      response0.headers()
        .firstValue("Header-0")
        .orElseThrow()
    );

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/xyz", req.path());
    }

    assertEquals(201, response1.statusCode());
    assertEquals(
      "Hello 1.",
      response1.body()
    );
    assertEquals(
      "ABC",
      response1.headers()
        .firstValue("Header-1")
        .orElseThrow()
    );
    assertEquals(
      "text/plain",
      response1.headers()
        .firstValue("Content-Type")
        .orElseThrow()
    );

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/xyz", req.path());
    }

    assertEquals(503, response2.statusCode());
    assertEquals(
      "No matching responses for method GET and path '/xyz'.",
      response2.body()
    );

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/xyz", req.path());
    }

    assertEquals(0, requests.size());
  }


  /**
   * A GET request doesn't match.
   *
   * @throws Exception On errors
   */

  @Test
  public void testGET_NoMatchPath()
    throws Exception
  {
    /* Arrange */

    this.server.addResponse()
      .forMethod("GET")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 0.")
      .withStatus(201)
      .withHeader("Header-0", "XYZ");

    assertEquals(1, this.server.responses().size());

    /* Act */

    final var request =
      HttpRequest.newBuilder(this.server.uri().resolve("abc"))
        .build();

    final var response0 =
      this.http.send(request, ofString());

    /* Assert */

    final var requests =
      new LinkedList<>(this.server.requestsReceived());

    assertEquals(503, response0.statusCode());
    assertEquals(
      "No matching responses for method GET and path '/abc'.",
      response0.body()
    );

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/abc", req.path());
    }

    assertEquals(0, requests.size());
  }

  /**
   * A GET request doesn't match.
   *
   * @throws Exception On errors
   */

  @Test
  public void testGET_NoMatchMethod()
    throws Exception
  {
    /* Arrange */

    this.server.addResponse()
      .forMethod("POST")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 0.")
      .withStatus(201)
      .withHeader("Header-0", "XYZ");

    assertEquals(1, this.server.responses().size());

    /* Act */

    final var request =
      HttpRequest.newBuilder(this.server.uri().resolve("xyz"))
        .build();

    final var response0 =
      this.http.send(request, ofString());

    /* Assert */

    final var requests =
      new LinkedList<>(this.server.requestsReceived());

    assertEquals(503, response0.statusCode());
    assertEquals(
      "No matching responses for method GET and path '/xyz'.",
      response0.body()
    );

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/xyz", req.path());
    }

    assertEquals(0, requests.size());
  }

  /**
   * Gzip compression works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testGET_Gzip()
    throws Exception
  {
    /* Arrange */

    this.server.enableGzip(true);

    this.server.addResponse()
      .forMethod("GET")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 0.")
      .withStatus(201)
      .withHeader("Header-0", "XYZ");

    assertEquals(1, this.server.responses().size());

    /* Act */

    final var request =
      HttpRequest.newBuilder(this.server.uri().resolve("xyz"))
        .setHeader("Accept-Encoding", "gzip")
        .build();

    final var response0 =
      this.http.send(request, ofByteArray());

    /* Assert */

    final var requests =
      new LinkedList<>(this.server.requestsReceived());

    assertEquals(201, response0.statusCode());

    try (var stream =
           new GZIPInputStream(new ByteArrayInputStream(response0.body()))) {
      assertEquals(
        "Hello 0.",
        new String(stream.readAllBytes(), UTF_8)
      );
    }

    {
      final var req = requests.remove(0);
      assertEquals("GET", req.method());
      assertEquals("/xyz", req.path());
    }

    assertEquals(0, requests.size());
  }

  /**
   * Test a series of POST requests.
   *
   * @throws Exception On errors
   */

  @Test
  public void testPOST_0()
    throws Exception
  {
    /* Arrange */

    this.server.addResponse()
      .forMethod("POST")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 0.")
      .withStatus(201)
      .withHeader("Header-0", "XYZ");

    this.server.addResponse()
      .forMethod("POST")
      .forPath("/xyz")
      .withContentType("text/plain")
      .withFixedText("Hello 1.")
      .withStatus(201)
      .withHeader("Header-1", "ABC");

    assertEquals(2, this.server.responses().size());

    assertEquals(
      List.of(
        "[Response [Method POST] [Path ^/xyz]]",
        "[Response [Method POST] [Path ^/xyz]]"
      ),
      this.server.responses()
        .stream()
        .map(Object::toString)
        .toList()
    );

    /* Act */

    final var request =
      HttpRequest.newBuilder(this.server.uri().resolve("xyz"))
        .POST(BodyPublishers.ofString("Hello."))
        .setHeader("Content-Type", "text/plain;encoding=utf-8")
        .build();

    final var response0 =
      this.http.send(request, ofString());

    final var response1 =
      this.http.send(request, ofString());

    final var response2 =
      this.http.send(request, ofString());

    /* Assert */

    final var requests =
      new LinkedList<>(this.server.requestsReceived());

    assertEquals(201, response0.statusCode());
    assertEquals(
      "Hello 0.",
      response0.body()
    );
    assertEquals(
      "text/plain",
      response0.headers()
        .firstValue("Content-Type")
        .orElseThrow()
    );
    assertEquals(
      "XYZ",
      response0.headers()
        .firstValue("Header-0")
        .orElseThrow()
    );

    {
      final var req = requests.remove(0);
      assertEquals("POST", req.method());
      assertEquals("/xyz", req.path());
      assertEquals(
        "Hello.",
        req.files().get("postData")
      );
    }

    assertEquals(201, response1.statusCode());
    assertEquals(
      "Hello 1.",
      response1.body()
    );
    assertEquals(
      "ABC",
      response1.headers()
        .firstValue("Header-1")
        .orElseThrow()
    );
    assertEquals(
      "text/plain",
      response1.headers()
        .firstValue("Content-Type")
        .orElseThrow()
    );

    {
      final var req = requests.remove(0);
      assertEquals("POST", req.method());
      assertEquals("/xyz", req.path());
      assertEquals(
        "Hello.",
        req.files().get("postData")
      );
    }

    assertEquals(503, response2.statusCode());
    assertEquals(
      "No matching responses for method POST and path '/xyz'.",
      response2.body()
    );

    {
      final var req = requests.remove(0);
      assertEquals("POST", req.method());
      assertEquals("/xyz", req.path());
      assertEquals(
        "Hello.",
        req.files().get("postData")
      );
    }

    assertEquals(0, requests.size());
  }

}
