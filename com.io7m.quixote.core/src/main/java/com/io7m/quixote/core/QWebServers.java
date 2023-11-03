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

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static fi.iki.elonen.NanoHTTPD.Response.Status.SERVICE_UNAVAILABLE;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static java.util.Locale.ROOT;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * The default web servers.
 */

public final class QWebServers implements QWebServerFactoryType
{
  /**
   * The default web servers.
   */

  public QWebServers()
  {

  }

  /**
   * Create a new web server that listens on the given port on localhost.
   *
   * @param port The port
   *
   * @return A new web server
   *
   * @throws IOException On errors
   */

  public static QWebServerType createServer(
    final int port)
    throws IOException
  {
    return new QWebServers().create(port);
  }

  /**
   * Create a new web server that listens on the given port on all available
   * addresses.
   *
   * @param port The port
   *
   * @return A new web server
   *
   * @throws IOException On errors
   */

  public static QWebServerType createServerForAll(
    final int port)
    throws IOException
  {
    return new QWebServers().createForAll(port);
  }

  /**
   * Create a new web server that listens on the given port on the given address.
   *
   * @param address The address
   * @param port    The port
   *
   * @return A new web server
   *
   * @throws IOException On errors
   */

  public static QWebServerType createServerForSpecific(
    final InetAddress address,
    final int port)
    throws IOException
  {
    return new QWebServers().createForSpecific(address, port);
  }


  @Override
  public QWebServerType create(
    final int port)
    throws IOException
  {
    return new QWebServer("localhost", port);
  }

  @Override
  public QWebServerType createForAll(
    final int port)
    throws IOException
  {
    return new QWebServer("[::]", port);
  }

  @Override
  public QWebServerType createForSpecific(
    final InetAddress address,
    final int port)
    throws IOException
  {
    return new QWebServer(address.getHostName(), port);
  }

  private record QWebRequestReceived(
    String method,
    String path,
    Map<String, String> headers,
    Map<String, String> files)
    implements QWebRequestReceivedType
  {
    private QWebRequestReceived
    {
      Objects.requireNonNull(method, "method");
      Objects.requireNonNull(path, "path");
      Objects.requireNonNull(headers, "headers");
      Objects.requireNonNull(files, "files");
    }
  }

  private static final class QWebServer extends NanoHTTPD
    implements QWebServerType
  {
    private final URI baseURI;
    private final LinkedList<QMutableResponse> responses;
    private final LinkedList<QWebRequestReceived> requests;
    private boolean gzipEnabled;

    QWebServer(
      final String hostName,
      final int port)
      throws IOException
    {
      super(
        Objects.requireNonNull(hostName, "hostName"),
        port
      );

      this.responses =
        new LinkedList<>();
      this.requests =
        new LinkedList<>();

      this.baseURI =
        URI.create(
          "http://%s:%d/"
            .formatted(
              this.getHostname(),
              Integer.valueOf(port))
        );

      this.start();
    }

    @Override
    public Response serve(
      final IHTTPSession session)
    {
      final var requestReceived =
        new QWebRequestReceived(
          session.getMethod().name(),
          session.getUri(),
          Map.copyOf(session.getHeaders()),
          new HashMap<>()
        );

      this.requests.add(requestReceived);

      try {
        session.parseBody(requestReceived.files);
      } catch (final Exception e) {
        return newFixedLengthResponse(
          SERVICE_UNAVAILABLE,
          "text/plain",
          String.format(
            "Failed response for method %s and path '%s': %s",
            session.getMethod(),
            session.getUri(),
            e)
        );
      }

      final var iterator =
        this.responses.iterator();

      while (iterator.hasNext()) {
        final var response = iterator.next();
        if (response.matches(session)) {
          iterator.remove();
          return response.httpResponse();
        }
      }

      return newFixedLengthResponse(
        SERVICE_UNAVAILABLE,
        "text/plain",
        String.format(
          "No matching responses for method %s and path '%s'.",
          session.getMethod(),
          session.getUri())
      );
    }

    @Override
    protected boolean useGzipWhenAccepted(
      final Response r)
    {
      return this.gzipEnabled;
    }

    @Override
    public void close()
    {
      super.stop();
    }

    @Override
    public URI uri()
    {
      return this.baseURI;
    }

    @Override
    public QWebServerType enableGzip(
      final boolean enable)
    {
      this.gzipEnabled = enable;
      return this;
    }

    @Override
    public QWebResponseType addResponse()
    {
      final var response = new QMutableResponse();
      this.responses.add(response);
      return response;
    }

    @Override
    public List<QWebResponseType> responses()
    {
      return List.copyOf(this.responses);
    }

    @Override
    public List<QWebRequestReceivedType> requestsReceived()
    {
      return List.copyOf(this.requests);
    }
  }

  private static final class QMutableResponse implements QWebResponseType
  {
    private final HashMap<String, String> responseHeaders;
    private int responseCode;
    private Pattern patternPath;
    private Pattern patternMethod;
    private InputStream responseData;
    private String contentType;
    private long contentLength;

    @Override
    public String toString()
    {
      return String.format(
        "[Response [Method %s] [Path %s]]",
        this.patternMethod,
        this.patternPath
      );
    }

    private QMutableResponse()
    {
      this.responseHeaders =
        new HashMap<>();
      this.patternMethod =
        Pattern.compile(".*", CASE_INSENSITIVE);
      this.patternPath =
        Pattern.compile("^/.*");

      this.contentType = "application/octet-stream";
      this.responseCode = 200;
      this.contentLength = -1L;
    }

    @Override
    public QWebResponseType forMethod(
      final String pattern)
    {
      Objects.requireNonNull(pattern, "pattern");
      this.patternMethod = Pattern.compile(pattern, CASE_INSENSITIVE);
      return this;
    }

    @Override
    public QWebResponseType forPath(
      final String pattern)
    {
      Objects.requireNonNull(pattern, "pattern");
      this.patternPath = Pattern.compile("^" + pattern);
      return this;
    }

    @Override
    public QWebResponseType withStatus(
      final int code)
    {
      this.responseCode = code;
      return this;
    }

    @Override
    public QWebResponseType withHeader(
      final String name,
      final String value)
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(value, "value");

      this.responseHeaders.put(name.toLowerCase(ROOT), value);
      return this;
    }

    @Override
    public QWebResponseType withData(
      final InputStream data)
    {
      this.responseData = Objects.requireNonNull(data, "data");
      return this;
    }

    @Override
    public QWebResponseType withContentType(
      final String type)
    {
      this.contentType = Objects.requireNonNull(type, "type");
      return this;
    }

    @Override
    public QWebResponseType withContentLength(
      final long size)
    {
      this.contentLength = size;
      return this;
    }

    public boolean matches(
      final NanoHTTPD.IHTTPSession session)
    {
      final var methodText =
        session.getMethod().name();
      final var pathText =
        session.getUri();

      final var methodMatches =
        this.patternMethod.matcher(methodText).matches();
      final var pathMatches =
        this.patternPath.matcher(pathText).matches();

      return methodMatches && pathMatches;
    }

    public NanoHTTPD.Response httpResponse()
    {
      final var response =
        newFixedLengthResponse(
          NanoHTTPD.Response.Status.lookup(this.responseCode),
          this.contentType,
          this.responseData,
          this.contentLength
        );

      for (final var entry : this.responseHeaders.entrySet()) {
        response.addHeader(entry.getKey(), entry.getValue());
      }

      return response;
    }
  }
}
