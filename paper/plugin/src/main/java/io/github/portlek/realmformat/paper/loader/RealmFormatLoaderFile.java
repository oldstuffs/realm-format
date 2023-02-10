package io.github.portlek.realmformat.paper.loader;

import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;

public final class RealmFormatLoaderFile implements RealmFormatLoader {

  @NotNull
  private final Path directory;

  public RealmFormatLoaderFile(@NotNull final Path directory) {
    this.directory = directory;
  }

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {}
}
