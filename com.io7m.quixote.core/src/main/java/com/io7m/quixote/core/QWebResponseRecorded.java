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

package com.io7m.quixote.core;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A recorded response.
 *
 * @param method     The method for which this response will be returned
 * @param path       The path for which this response will be returned
 * @param statusCode The status code
 * @param headers    The headers
 * @param content    The content
 */

public record QWebResponseRecorded(
  Pattern method,
  Pattern path,
  int statusCode,
  Map<String, String> headers,
  byte[] content)
{
  /**
   * A recorded response.
   *
   * @param method     The method for which this response will be returned
   * @param path       The path for which this response will be returned
   * @param statusCode The status code
   * @param headers    The headers
   * @param content    The content
   */

  public QWebResponseRecorded
  {
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(path, "path");
    headers = Map.copyOf(headers);
    content = content.clone();
  }
}
