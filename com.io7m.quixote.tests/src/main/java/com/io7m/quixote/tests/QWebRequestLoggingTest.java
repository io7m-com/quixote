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

import com.io7m.quixote.core.QWebRequestLogging;
import com.io7m.quixote.core.QWebRequestReceivedType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public final class QWebRequestLoggingTest
{
  private ByteArrayOutputStream out;
  private QWebRequestReceivedType request;

  @BeforeEach
  public void setup()
  {
    this.out =
      new ByteArrayOutputStream();
    this.request =
      Mockito.mock(QWebRequestReceivedType.class);
  }

  @Test
  public void testSimple()
    throws IOException
  {
    when(this.request.method())
      .thenReturn("POST");
    when(this.request.path())
      .thenReturn("/x/y/z");
    when(this.request.headers())
      .thenReturn(Map.of());
    when(this.request.files())
      .thenReturn(Map.of());

    QWebRequestLogging.append(this.out, this.request);
    QWebRequestLogging.append(this.out, this.request);
    QWebRequestLogging.append(this.out, this.request);

    final var in =
      new ByteArrayInputStream(this.out.toByteArray());

    {
      final var r = QWebRequestLogging.read(in);
      assertEquals("/x/y/z", r.path());
      assertEquals("POST", r.method());
      assertEquals(Map.of(), r.headers());
      assertEquals(Map.of(), r.files());
    }

    {
      final var r = QWebRequestLogging.read(in);
      assertEquals("/x/y/z", r.path());
      assertEquals("POST", r.method());
      assertEquals(Map.of(), r.headers());
      assertEquals(Map.of(), r.files());
    }

    {
      final var r = QWebRequestLogging.read(in);
      assertEquals("/x/y/z", r.path());
      assertEquals("POST", r.method());
      assertEquals(Map.of(), r.headers());
      assertEquals(Map.of(), r.files());
    }
  }
}
