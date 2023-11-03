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


package com.io7m.quixote.core;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

/**
 * Functions to conveniently find public IP addresses.
 */

public final class QWebServerAddresses
{
  private QWebServerAddresses()
  {

  }

  /**
   * Find a usable public IP address on the current machine.
   *
   * @return The address, if any
   */

  public static Optional<InetAddress> findPublicIP()
  {
    final Iterator<NetworkInterface> interfaces;
    try {
      interfaces = NetworkInterface.getNetworkInterfaces().asIterator();
    } catch (final SocketException e) {
      return Optional.empty();
    }

    final var results =
      new HashSet<InetAddress>();

    while (interfaces.hasNext()) {
      final var inter = interfaces.next();
      try {
        if (inter.isLoopback()) {
          continue;
        }
        if (inter.isVirtual()) {
          continue;
        }
        if (!inter.isUp()) {
          continue;
        }
      } catch (final SocketException e) {
        // Ignoring.
      }

      final var addresses =
        inter.inetAddresses()
          .toList();

      for (final var address : addresses) {
        if (address.isLoopbackAddress()) {
          continue;
        }
        if (address.isLinkLocalAddress()) {
          continue;
        }
        results.add(address);
      }
    }

    return results.stream()
      .max(QWebServerAddresses::compareAddresses);
  }

  private static int compareAddresses(
    final InetAddress o1,
    final InetAddress o2)
  {
    return switch (o1) {
      case final Inet4Address o41 -> {
        switch (o2) {
          case final Inet4Address o42 -> {
            yield 0;
          }
          case final Inet6Address o62 -> {
            yield -1;
          }
          default -> throw new IllegalStateException("Unexpected value: " + o2);
        }
      }
      case final Inet6Address o61 -> {
        switch (o2) {
          case final Inet4Address o42 -> {
            yield 1;
          }
          case final Inet6Address o62 -> {
            yield 0;
          }
          default -> throw new IllegalStateException("Unexpected value: " + o2);
        }
      }
      default -> throw new IllegalStateException("Unexpected value: " + o1);
    };
  }
}
