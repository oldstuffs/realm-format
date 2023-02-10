package io.github.portlek.realmformat.paper.loader;

import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.internal.misc.MongoCredential;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;

public final class RealmFormatLoaderMongo implements RealmFormatLoader {

  @NotNull
  private final MongoCredential credential;

  public RealmFormatLoaderMongo(@NotNull final MongoCredential credential) {
    this.credential = credential;
  }

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {}
}
