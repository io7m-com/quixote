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

import org.osgi.annotation.versioning.ProviderType;

import java.io.Closeable;
import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

/**
 * A web server.
 */

@ProviderType
public interface QWebServerType extends Closeable
{
  /**
   * @return The web server's base URI, including a trailing slash
   */

  URI uri();

  /**
   * Enable GZIP if clients support it.
   *
   * @param enable {@code true} if GZIP should enabled
   *
   * @return this
   */

  QWebServerType enableGzip(
    boolean enable);

  /**
   * Add a new response.
   *
   * @return The response
   */

  QWebResponseType addResponse();

  /**
   * @return A read-only snapshot of the current list of responses in the order
   * they were added
   */

  List<QWebResponseType> responses();

  /**
   * @return A read-only snapshot of the current list of requests that have been
   * received, in the order they were received
   */

  List<QWebRequestReceivedType> requestsReceived();

  /**
   * Set the callback that will be evaluated on each request.
   *
   * @param onRequest The request receiver
   */

  void setRequestCallback(
    Consumer<QWebRequestReceivedType> onRequest);
}
