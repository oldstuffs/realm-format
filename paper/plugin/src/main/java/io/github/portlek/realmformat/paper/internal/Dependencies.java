package io.github.portlek.realmformat.paper.internal;

import io.github.portlek.realmformat.paper.internal.exception.LibrariesCouldNotDownloadedException;
import io.github.portlek.smol.app.builder.ApplicationBuilder;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface Dependencies {
  static void load(@NotNull final Path downloadDirectory) {
    try {
      ApplicationBuilder.appending("RealmFormat").downloadDirectoryPath(downloadDirectory).build();
    } catch (final Exception e) {
      throw new LibrariesCouldNotDownloadedException(e);
    }
  }
}
