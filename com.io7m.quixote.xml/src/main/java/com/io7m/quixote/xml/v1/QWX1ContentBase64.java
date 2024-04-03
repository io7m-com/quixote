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

import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * XML element handler.
 */

public final class QWX1ContentBase64
  implements BTElementHandlerType<Object, byte[]>
{
  private static final Pattern WHITESPACE =
    Pattern.compile("\\s+");

  private ByteArrayOutputStream bytes;

  /**
   * XML element handler.
   *
   * @param context The parse context
   */

  public QWX1ContentBase64(
    final BTElementParsingContextType context)
  {
    this.bytes = new ByteArrayOutputStream();
  }

  @Override
  public void onCharacters(
    final BTElementParsingContextType context,
    final char[] data,
    final int offset,
    final int length)
  {
    final var rawText =
      new String(data, offset, length);
    final var trimmedText =
      WHITESPACE.matcher(rawText).replaceAll("");
    final var decoded =
      Base64.getDecoder()
        .decode(trimmedText);

    this.bytes.writeBytes(decoded);
  }

  @Override
  public byte[] onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.bytes.toByteArray();
  }
}
