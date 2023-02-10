package io.github.portlek.realmformat.paper.loader;

import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import java.util.Map;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

public final class RealmFormatLoaderMap implements Map<String, RealmFormatLoader> {

  @NotNull
  @Delegate
  private final Map<String, RealmFormatLoader> delegate;

  public RealmFormatLoaderMap(@NotNull final Map<String, RealmFormatLoader> delegate) {
    this.delegate = delegate;
  }
}
