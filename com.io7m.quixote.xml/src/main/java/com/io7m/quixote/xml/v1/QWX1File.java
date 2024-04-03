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
import com.io7m.blackthorne.core.BTIgnoreUnrecognizedElements;
import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.blackthorne.core.Blackthorne;
import com.io7m.quixote.core.QWebConfiguration;
import com.io7m.quixote.core.QWebResponseRecorded;
import com.io7m.quixote.core.QWebServerConfiguration;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * XML element handler.
 */

public final class QWX1File
  implements BTElementHandlerType<Object, QWebConfiguration>
{
  private List<QWebResponseRecorded> responses;
  private int port;
  private boolean gzip;
  private String hostname;

  /**
   * XML element handler.
   *
   * @param context The parse context
   */

  public QWX1File(
    final BTElementParsingContextType context)
  {
    this.responses =
      new ArrayList<>();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        QWX1.element("Responses"),
        Blackthorne.forListMono(
          QWX1.element("Responses"),
          QWX1.element("Response"),
          QWX1Response::new,
          BTIgnoreUnrecognizedElements.DO_NOT_IGNORE_UNRECOGNIZED_ELEMENTS
        )
      )
    );
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.hostname =
      Objects.requireNonNullElse(
        attributes.getValue("HostName"),
        "localhost"
      );
    this.port =
      Integer.parseUnsignedInt(attributes.getValue("Port"));
    this.gzip =
      Boolean.parseBoolean(attributes.getValue("GZIP"));
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final List<?> responseList -> {
        if (!responseList.isEmpty()) {
          final var r = responseList.get(0);
          switch (r) {
            case final QWebResponseRecorded rec -> {
              this.responses.addAll(
                (Collection<? extends QWebResponseRecorded>) responseList
              );
            }
            default -> {
              throw new IllegalStateException("Unexpected value: " + r);
            }
          }
        }
      }

      default -> {
        throw new IllegalStateException("Unexpected value: " + result);
      }
    }
  }

  @Override
  public QWebConfiguration onElementFinished(
    final BTElementParsingContextType context)
  {
    return new QWebConfiguration(
      new QWebServerConfiguration(
        this.hostname,
        this.port,
        this.gzip
      ),
      this.responses
    );
  }
}
