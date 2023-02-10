package io.github.portlek.realmformat.paper.loader;

import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.internal.misc.MongoCredential;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;

public final class RealmFormatLoaderMongo implements RealmFormatLoader {

  @NotNull
  private final MongoCredential credential;

  public RealmFormatLoaderMongo(@NotNull final MongoCredential credential) {
    this.credential = credential;
  }

  @Override
  public byte[] load(@NotNull final String worldName, final boolean readOnly) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void save(
    @NotNull final String worldName,
    final byte@NotNull[] serialized,
    final boolean lock
  ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unlock(@NotNull final String worldName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean locked(@NotNull final String worldName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(@NotNull final String worldName) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<String> list() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(@NotNull final String worldName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {}
}
