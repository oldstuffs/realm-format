package io.github.portlek.realmformat.paper.loader;

import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.api.RealmFormatManager;
import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;

public final class RealmFormatLoaderMongo implements RealmFormatLoader {

  private final RealmFormatConfig config = Services.load(RealmFormatConfig.class);

  private final RealmFormatManager manager = Services.load(RealmFormatManager.class);

  @Override
  public void delete(@NotNull final String worldName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(@NotNull final String worldName) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Collection<String> list() {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte[] load(@NotNull final String worldName, final boolean readOnly) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean locked(@NotNull final String worldName) {
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
  public void setup(@NotNull final TerminableConsumer consumer) {
    this.manager.registerLoader("mongo", this);
  }
}
