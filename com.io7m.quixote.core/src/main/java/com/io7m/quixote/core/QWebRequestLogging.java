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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Functions to read and write logs in the binary request log format.
 */

public final class QWebRequestLogging
{
  private static final byte[] HEADER;

  static {
    try (var out = new ByteArrayOutputStream()) {
      out.writeBytes("QUIXOTE!".getBytes(UTF_8));
      out.write('\0');
      out.write('\0');
      out.write('\0');
      out.write('\1');
      out.flush();
      HEADER = out.toByteArray();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private QWebRequestLogging()
  {

  }

  /**
   * Append a request to the given log.
   *
   * @param outputLog The output stream
   * @param request   The request
   *
   * @throws IOException On I/O errors
   */

  public static void append(
    final OutputStream outputLog,
    final QWebRequestReceivedType request)
    throws IOException
  {
    serializeRequest(outputLog, request);
  }

  private static void serializeRequest(
    final OutputStream outputLog,
    final QWebRequestReceivedType request)
    throws IOException
  {
    outputLog.write(HEADER);
    outputLog.flush();

    final var properties = new Properties();
    properties.setProperty("Info.Path", request.path());
    properties.setProperty("Info.Method", request.method());

    for (final var entry : request.headers().entrySet()) {
      properties.setProperty(
        "Header.%s".formatted(entry.getKey()),
        entry.getValue()
      );
    }

    for (final var entry : request.files().entrySet()) {
      properties.setProperty(
        "File.%s".formatted(entry.getKey()),
        entry.getValue()
      );
    }

    try (var textStream = new ByteArrayOutputStream()) {
      properties.storeToXML(textStream, "", UTF_8);
      textStream.flush();
      writeBytes(outputLog, textStream.toByteArray());
    }

    outputLog.flush();
  }

  private static void writeBytes(
    final OutputStream outputLog,
    final byte[] data)
    throws IOException
  {
    final var lengthArray =
      new byte[8];
    final var length =
      ByteBuffer.wrap(lengthArray)
        .order(ByteOrder.BIG_ENDIAN);

    length.putLong(0, Integer.toUnsignedLong(data.length));
    outputLog.write(lengthArray);
    outputLog.write(data);
  }

  /**
   * Read a request from the given log.
   *
   * @param inputLog The input stream
   *
   * @return The parsed request properties
   *
   * @throws IOException On I/O errors
   */

  public static QWebRequestReceivedType read(
    final InputStream inputLog)
    throws IOException
  {
    final var header =
      inputLog.readNBytes(12);

    if (!Arrays.equals(header, HEADER)) {
      throw new IOException(
        "Invalid header: %s".formatted(HexFormat.of().formatHex(header))
      );
    }

    final var lengthArray =
      inputLog.readNBytes(8);

    final var lengthBuffer =
      ByteBuffer.wrap(lengthArray)
        .order(ByteOrder.BIG_ENDIAN);

    final var length =
      lengthBuffer.getLong(0);

    final var body =
      inputLog.readNBytes(Math.toIntExact(length));

    try (var bodyStream = new ByteArrayInputStream(body)) {
      final var properties = new Properties();
      properties.loadFromXML(bodyStream);
      return new ImmutableRequest(properties);
    }
  }

  private record ImmutableRequest(
    Properties properties)
    implements QWebRequestReceivedType
  {

    @Override
    public String method()
    {
      return this.properties.getProperty("Info.Method");
    }

    @Override
    public String path()
    {
      return this.properties.getProperty("Info.Path");

    }

    @Override
    public Map<String, String> headers()
    {
      return this.properties.stringPropertyNames()
        .stream()
        .filter(x -> x.startsWith("Header."))
        .map(x -> Map.entry(x, this.properties.getProperty(x)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, String> files()
    {
      return this.properties.stringPropertyNames()
        .stream()
        .filter(x -> x.startsWith("File."))
        .map(x -> Map.entry(x, this.properties.getProperty(x)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
  }
}
