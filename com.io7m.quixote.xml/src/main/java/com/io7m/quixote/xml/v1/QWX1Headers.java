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

import java.util.HashMap;
import java.util.Map;

/**
 * XML element handler.
 */

public final class QWX1Headers
  implements BTElementHandlerType<Map.Entry<String, String>, Map<String, String>>
{
  private final HashMap<String, String> entries;

  /**
   * XML element handler.
   *
   * @param context The parse context
   */

  public QWX1Headers(
    final BTElementParsingContextType context)
  {
    this.entries = new HashMap<>();
  }

  @Override
  public Map<
    BTQualifiedName,
    BTElementHandlerConstructorType<?, ? extends Map.Entry<String, String>>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        QWX1.element("Header"),
        QWX1Header::new
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Map.Entry<String, String> result)
  {
    this.entries.put(result.getKey(), result.getValue());
  }

  @Override
  public Map<String, String> onElementFinished(
    final BTElementParsingContextType context)
  {
    return Map.copyOf(this.entries);
  }
}
