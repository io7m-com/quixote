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


package com.io7m.quixote.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A queued response.
 */

public interface QWebResponseType
{
  /**
   * Set the HTTP method against this response will match.
   *
   * @param pattern The method regular expression (case insensitive)
   *
   * @return this
   */

  QWebResponseType forMethod(
    String pattern);

  /**
   * Set the path against this response will match. The expression is
   * automatically adjusted as if it were specified to begin with a '^'
   * character, meaning that it's not necessary to manually specify that the
   * pattern is matched against the start of the path string.
   *
   * @param pattern The path regular expression
   *
   * @return this
   */

  QWebResponseType forPath(
    String pattern);

  /**
   * The status code that will be returned.
   *
   * @param code The code
   *
   * @return this
   */

  QWebResponseType withStatus(
    int code);

  /**
   * A header that will be included in the response.
   *
   * @param name  The header name
   * @param value The header value
   *
   * @return this
   */

  QWebResponseType withHeader(
    String name,
    String value);

  /**
   * A stream of data that will be returned in the response.
   *
   * @param data The data
   *
   * @return this
   */

  QWebResponseType withData(
    InputStream data);

  /**
   * The content type that will be returned in the response.
   *
   * @param type The content type
   *
   * @return this
   */

  QWebResponseType withContentType(
    String type);

  /**
   * The content length that will be returned in the response.
   *
   * @param size The content length
   *
   * @return this
   */

  QWebResponseType withContentLength(
    long size);

  /**
   * The data that will be returned in the response.
   *
   * @param data The data
   *
   * @return this
   */

  default QWebResponseType withFixedData(
    final byte[] data)
  {
    return this.withData(new ByteArrayInputStream(data))
      .withContentLength(Integer.toUnsignedLong(data.length));
  }

  /**
   * The data that will be returned in the response.
   *
   * @param data The data
   *
   * @return this
   */

  default QWebResponseType withFixedText(
    final String data)
  {
    final var bytes =
      data.getBytes(StandardCharsets.UTF_8);
    return this.withFixedData(bytes)
      .withContentLength(bytes.length);
  }
}
