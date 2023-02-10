package io.github.portlek.realmformat.paper.module;

import io.github.portlek.realmformat.paper.api.event.RealmFormatLoaderLoadEvent;
import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import io.github.portlek.realmformat.paper.loader.RealmFormatLoaderFile;
import io.github.portlek.realmformat.paper.loader.RealmFormatLoaderMap;
import io.github.portlek.realmformat.paper.loader.RealmFormatLoaderMongo;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

public final class RealmFormatLoaderModule implements TerminableModule {

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {
    final var config = Services.load(RealmFormatConfig.class);
    final var loaders = Services.provide(
      RealmFormatLoaderMap.class,
      new RealmFormatLoaderMap(new ConcurrentHashMap<>())
    );
    loaders
      .compute(
        "file",
        (__, ___) -> new RealmFormatLoaderFile(new File(config.fileLoaderPath()).toPath())
      )
      .bindModuleWith(consumer);
    loaders
      .compute("mongo", (__, ___) -> new RealmFormatLoaderMongo(config.mongo()))
      .bindModuleWith(consumer);
    new RealmFormatLoaderLoadEvent().callEvent();
  }
}
