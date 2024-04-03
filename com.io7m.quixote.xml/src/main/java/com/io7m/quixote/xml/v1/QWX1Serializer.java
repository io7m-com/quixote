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


package com.io7m.quixote.xml.v1;

import com.io7m.quixote.core.QWebConfiguration;
import com.io7m.quixote.core.QWebResponseRecorded;
import com.io7m.quixote.xml.QWebSchemas;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * A serializer for configurations.
 */

public final class QWX1Serializer
{
  private final XMLStreamWriter output;
  private final XMLOutputFactory outputs;
  private final String ns;

  /**
   * A serializer for configurations.
   *
   * @param inOutput The output stream
   *
   * @throws XMLStreamException On errors
   */

  public QWX1Serializer(
    final OutputStream inOutput)
    throws XMLStreamException
  {
    this.outputs =
      XMLOutputFactory.newFactory();
    this.output =
      this.outputs.createXMLStreamWriter(inOutput, "UTF-8");
    this.ns =
      QWebSchemas.schema1().namespace().toString();
  }

  /**
   * Execute the serializer.
   *
   * @param configuration The configuration
   *
   * @throws XMLStreamException On errors
   */

  public void execute(
    final QWebConfiguration configuration)
    throws XMLStreamException
  {
    this.output.writeStartDocument("UTF-8", "1.0");
    this.serializeConfiguration(configuration);
    this.output.writeEndDocument();
  }

  private void serializeConfiguration(
    final QWebConfiguration configuration)
    throws XMLStreamException
  {
    this.output.writeStartElement("Configuration");
    this.output.writeDefaultNamespace(this.ns);

    this.output.writeAttribute(
      "HostName",
      configuration.serverConfiguration().hostName()
    );
    this.output.writeAttribute(
      "Port",
      Integer.toUnsignedString(configuration.serverConfiguration().port())
    );
    this.output.writeAttribute(
      "GZIP",
      Boolean.toString(configuration.serverConfiguration().enableGZIP())
    );

    this.serializeResponses(configuration.responses());
    this.output.writeEndElement();
  }

  private void serializeResponses(
    final List<QWebResponseRecorded> responses)
    throws XMLStreamException
  {
    this.output.writeStartElement("Responses");

    for (final var response : responses) {
      this.serializeResponse(response);
    }

    this.output.writeEndElement();
  }

  private void serializeResponse(
    final QWebResponseRecorded response)
    throws XMLStreamException
  {
    this.output.writeStartElement("Response");

    this.output.writeAttribute(
      "Path",
      response.path().pattern());
    this.output.writeAttribute(
      "Method",
      response.method().pattern());
    this.output.writeAttribute(
      "Status",
      Integer.toUnsignedString(response.statusCode()));

    this.serializeHeaders(response.headers());
    this.serializeContent(response.content());

    this.output.writeEndElement();
  }

  private void serializeContent(
    final byte[] content)
    throws XMLStreamException
  {
    this.output.writeStartElement("ContentBase64");
    this.output.writeCData(Base64.getEncoder().encodeToString(content));
    this.output.writeEndElement();
  }

  private void serializeHeaders(
    final Map<String, String> headers)
    throws XMLStreamException
  {
    this.output.writeStartElement("Headers");

    final var keys = new TreeSet<>(headers.keySet());
    for (final var key : keys) {
      final var value = headers.get(key);
      this.output.writeStartElement("Header");
      this.output.writeAttribute("Name", key);
      this.output.writeAttribute("Value", value);
      this.output.writeEndElement();
    }

    this.output.writeEndElement();
  }
}
