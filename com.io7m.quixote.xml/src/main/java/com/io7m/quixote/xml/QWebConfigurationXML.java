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


package com.io7m.quixote.xml;

import com.io7m.anethum.api.ParseSeverity;
import com.io7m.anethum.api.ParseStatus;
import com.io7m.anethum.api.ParsingException;
import com.io7m.anethum.api.SerializationException;
import com.io7m.blackthorne.core.BTException;
import com.io7m.blackthorne.core.BTParseError;
import com.io7m.blackthorne.core.BTPreserveLexical;
import com.io7m.blackthorne.jxe.BlackthorneJXE;
import com.io7m.quixote.core.QWebConfiguration;
import com.io7m.quixote.xml.v1.QWX1;
import com.io7m.quixote.xml.v1.QWX1File;
import com.io7m.quixote.xml.v1.QWX1Serializer;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Functions to handle XML configurations.
 */

public final class QWebConfigurationXML
{
  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.CREATE,
    StandardOpenOption.WRITE,
    StandardOpenOption.TRUNCATE_EXISTING,
  };

  private QWebConfigurationXML()
  {

  }

  /**
   * Parse a configuration file.
   *
   * @param source          The source
   * @param stream          The stream
   * @param preserveLexical The lexical preservation flag
   * @param statusConsumer  The status consume
   *
   * @return A parsed configuration
   *
   * @throws ParsingException On errors
   */

  public static QWebConfiguration parse(
    final URI source,
    final InputStream stream,
    final BTPreserveLexical preserveLexical,
    final Consumer<ParseStatus> statusConsumer)
    throws ParsingException
  {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(stream, "stream");
    Objects.requireNonNull(preserveLexical, "preserveLexical");
    Objects.requireNonNull(statusConsumer, "statusConsumer");

    try {
      return BlackthorneJXE.parse(
        source,
        stream,
        Map.ofEntries(
          Map.entry(
            QWX1.element("Configuration"),
            QWX1File::new
          )
        ),
        QWebSchemas.schemas(),
        preserveLexical
      );
    } catch (final BTException e) {
      final var statuses =
        e.errors()
          .stream()
          .map(QWebConfigurationXML::mapParseError)
          .toList();

      for (final var status : statuses) {
        statusConsumer.accept(status);
      }

      throw new ParsingException(e.getMessage(), List.copyOf(statuses));
    }
  }

  /**
   * Serialize a configuration file.
   *
   * @param stream        The output stream
   * @param configuration The server configuration
   *
   * @throws SerializationException On errors
   */

  public static void serialize(
    final OutputStream stream,
    final QWebConfiguration configuration)
    throws SerializationException
  {
    try {
      new QWX1Serializer(stream).execute(configuration);
    } catch (final XMLStreamException e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  /**
   * Serialize a configuration file.
   *
   * @param file          The output file
   * @param configuration The server configuration
   *
   * @throws SerializationException On errors
   */

  public static void serialize(
    final Path file,
    final QWebConfiguration configuration)
    throws SerializationException
  {
    try (var output = Files.newOutputStream(file, OPEN_OPTIONS)) {
      serialize(output, configuration);
      output.flush();
    } catch (final IOException e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  private static ParseStatus mapParseError(
    final BTParseError error)
  {
    return ParseStatus.builder("parse-error", error.message())
      .withSeverity(mapSeverity(error.severity()))
      .withLexical(error.lexical())
      .build();
  }

  private static ParseSeverity mapSeverity(
    final BTParseError.Severity severity)
  {
    return switch (severity) {
      case ERROR -> ParseSeverity.PARSE_ERROR;
      case WARNING -> ParseSeverity.PARSE_WARNING;
    };
  }
}
