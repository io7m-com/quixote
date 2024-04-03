/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.anethum.api.ParsingException;
import com.io7m.blackthorne.core.BTPreserveLexical;
import com.io7m.quixote.xml.QWebConfigurationXML;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class QWebConfigurationXMLTest
{
  /**
   * Configuration parsing.
   *
   * @throws Exception On errors
   */

  @Test
  public void testConfig0()
    throws Exception
  {
    final var c =
      QWebConfigurationXML.parse(
        URI.create("urn:stdin"),
        resource("conf-0.xml"),
        BTPreserveLexical.DISCARD_LEXICAL_INFORMATION,
        status -> {
        }
      );

    assertEquals(20001, c.serverConfiguration().port());
    assertTrue(c.serverConfiguration().enableGZIP());

    {
      final var r = c.responses().get(0);
      assertEquals(200, r.statusCode());
      assertEquals("GET", r.method().pattern());
      assertEquals("/", r.path().pattern());
      assertEquals(Map.ofEntries(
        Map.entry("Content-Type", "application/octet-stream")
      ), r.headers());
      assertEquals(
        new String(
          resource("README-LICENSE.txt").readAllBytes(),
          StandardCharsets.UTF_8
        ).trim(),
        new String(
          r.content(),
          StandardCharsets.UTF_8
        ).trim()
      );
    }

    assertEquals(1, c.responses().size());
  }

  /**
   * Configuration parsing.
   *
   * @throws Exception On errors
   */

  @Test
  public void testConfig1()
    throws Exception
  {
    final var c =
      QWebConfigurationXML.parse(
        URI.create("urn:stdin"),
        resource("conf-1.xml"),
        BTPreserveLexical.DISCARD_LEXICAL_INFORMATION,
        status -> {
        }
      );

    assertEquals(20001, c.serverConfiguration().port());
    assertTrue(c.serverConfiguration().enableGZIP());

    {
      final var r = c.responses().get(0);
      assertEquals(200, r.statusCode());
      assertEquals("GET", r.method().pattern());
      assertEquals("/", r.path().pattern());
      assertEquals(Map.ofEntries(
        Map.entry("Content-Type", "application/octet-stream")
      ), r.headers());
      assertEquals(
        new String(
          resource("README-LICENSE.txt").readAllBytes(),
          StandardCharsets.UTF_8
        ).trim(),
        new String(
          r.content(),
          StandardCharsets.UTF_8
        ).trim()
      );
    }

    assertEquals(1, c.responses().size());
  }

  /**
   * Configuration parsing.
   */

  @TestFactory
  public Stream<DynamicTest> testParseErrors()
  {
    return Stream.of(
      "conf-error-0.xml",
      "conf-error-1.xml"
    ).map(QWebConfigurationXMLTest::testParseError);
  }

  private static DynamicTest testParseError(
    final String name)
  {
    return DynamicTest.dynamicTest(
      "testParseError_%s".formatted(name),
      () -> {
        Assertions.assertThrows(ParsingException.class, () -> {
          QWebConfigurationXML.parse(
            URI.create("urn:stdin"),
            resource(name),
            BTPreserveLexical.DISCARD_LEXICAL_INFORMATION,
            status -> {

            }
          );
        });
      });
  }

  private static InputStream resource(
    final String name)
    throws Exception
  {
    final var path =
      "/com/io7m/quixote/tests/%s".formatted(name);
    final var url =
      QWebConfigurationXMLTest.class.getResource(path);

    return url.openStream();
  }
}
