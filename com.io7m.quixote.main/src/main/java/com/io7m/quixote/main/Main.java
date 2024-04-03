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


package com.io7m.quixote.main;

import com.io7m.anethum.slf4j.ParseStatusLogging;
import com.io7m.blackthorne.core.BTPreserveLexical;
import com.io7m.quixote.core.QWebConfiguration;
import com.io7m.quixote.core.QWebRequestLogging;
import com.io7m.quixote.core.QWebServers;
import com.io7m.quixote.xml.QWebConfigurationXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Server main entry point.
 */

public final class Main
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Main.class);

  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.CREATE,
    StandardOpenOption.WRITE,
    StandardOpenOption.APPEND,
  };

  private Main()
  {

  }

  /**
   * Server main entry point.
   *
   * @param args The command-line arguments
   *
   * @throws Exception On errors
   */

  public static void main(
    final String[] args)
    throws Exception
  {
    if (args.length == 1) {
      if (Objects.equals(args[0], "help")) {
        LOG.info("Self-check succeeded.");
        return;
      }
    }

    if (args.length != 2) {
      LOG.info("Usage: input.xml output.bin");
      throw new IllegalArgumentException(
        "Missing required command-line arguments.");
    }

    final var inputFile =
      Paths.get(args[0]).toAbsolutePath();
    final var outputFile =
      Paths.get(args[1]).toAbsolutePath();

    final QWebConfiguration configuration;
    try (var stream = Files.newInputStream(inputFile)) {
      configuration =
        QWebConfigurationXML.parse(
          inputFile.toUri(),
          stream,
          BTPreserveLexical.PRESERVE_LEXICAL_INFORMATION,
          status -> {
            ParseStatusLogging.logWithAll(LOG, status);
          });
    }

    try (var outputLog =
           Files.newOutputStream(outputFile, OPEN_OPTIONS)) {
      try (var server =
             QWebServers.createServerForConfiguration(configuration)) {

        LOG.info("Quixote running at {}", server.uri());

        server.setRequestCallback(r -> {
          try {
            QWebRequestLogging.append(outputLog, r);
          } catch (final IOException e) {
            LOG.error("Failed to write output log: ", e);
          }
        });

        while (true) {
          try {
            Thread.sleep(1_000L);
          } catch (final InterruptedException e) {
            return;
          }
        }
      }
    }
  }
}
