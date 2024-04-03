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

import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.quixote.core.QWebResponseRecorded;
import org.xml.sax.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * XML element handler.
 */

public final class QWX1Response
  implements BTElementHandlerType<Object, QWebResponseRecorded>
{
  private final Map<String, String> headers;
  private Pattern path;
  private byte[] content;
  private Pattern method;
  private int statusCode;

  /**
   * XML element handler.
   *
   * @param context The parse context
   */

  public QWX1Response(
    final BTElementParsingContextType context)
  {
    this.headers = new HashMap<>();
    this.headers.put("Content-Type", "application/octet-stream");

    this.statusCode =
      200;
    this.content =
      new byte[0];
    this.method =
      Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
    this.path =
      Pattern.compile("^/.*", Pattern.CASE_INSENSITIVE);
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        QWX1.element("Headers"),
        QWX1Headers::new
      ),
      Map.entry(
        QWX1.element("ContentBase64"),
        QWX1ContentBase64::new
      ),
      Map.entry(
        QWX1.element("ContentUTF8"),
        QWX1ContentUTF8::new
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final Map<?, ?> m -> {
        this.headers.putAll((Map<? extends String, ? extends String>) m);
      }

      case final String data -> {
        this.content = data.getBytes(StandardCharsets.UTF_8);
      }

      case final byte[] data -> {
        this.content = data;
      }

      default -> {
        throw new IllegalStateException("Unexpected value: " + result);
      }
    }
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.method =
      Pattern.compile(
        Objects.requireNonNullElse(
          attributes.getValue("Method"),
          ".*"
        ),
        Pattern.CASE_INSENSITIVE
      );
    this.path =
      Pattern.compile(
        Objects.requireNonNullElse(
          attributes.getValue("Path"),
          "^/.*"
        ),
        Pattern.CASE_INSENSITIVE
      );
    this.statusCode =
      Integer.parseUnsignedInt(attributes.getValue("Status"));
  }

  @Override
  public QWebResponseRecorded onElementFinished(
    final BTElementParsingContextType context)
  {
    return new QWebResponseRecorded(
      this.method,
      this.path,
      this.statusCode,
      this.headers,
      this.content
    );
  }
}
