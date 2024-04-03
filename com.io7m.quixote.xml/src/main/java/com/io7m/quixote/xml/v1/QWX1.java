/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.quixote.xml.QWebSchemas;

/**
 * Functions over v1 elements.
 */

public final class QWX1
{
  private QWX1()
  {

  }

  /**
   * The element with the given name.
   *
   * @param localName The local name
   *
   * @return The qualified name
   */

  public static BTQualifiedName element(
    final String localName)
  {
    return BTQualifiedName.of(
      QWebSchemas.schema1().namespace().toString(),
      localName
    );
  }

}
