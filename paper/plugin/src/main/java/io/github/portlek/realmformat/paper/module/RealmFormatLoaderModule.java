package io.github.portlek.realmformat.paper.module;

import io.github.portlek.realmformat.paper.api.event.RealmFormatLoaderLoadEvent;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import io.github.portlek.realmformat.paper.loader.RealmFormatLoaderFile;
import io.github.portlek.realmformat.paper.loader.RealmFormatLoaderMongo;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

public final class RealmFormatLoaderModule implements TerminableModule {

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {
    Services.load(RealmFormatLoaderFile.class).bindModuleWith(consumer);
    Services.load(RealmFormatLoaderMongo.class).bindModuleWith(consumer);
    new RealmFormatLoaderLoadEvent().callEvent();
  }
}
