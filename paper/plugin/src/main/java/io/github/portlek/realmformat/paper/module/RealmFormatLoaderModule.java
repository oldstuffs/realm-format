package io.github.portlek.realmformat.paper.module;

import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

public final class RealmFormatLoaderModule implements TerminableModule {

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {
    final var config = Services.load(RealmFormatConfig.class);
  }
}
