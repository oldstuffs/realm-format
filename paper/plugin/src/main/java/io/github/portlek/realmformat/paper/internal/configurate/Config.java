package io.github.portlek.realmformat.paper.internal.configurate;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;

public interface Config {
  @NotNull
  ConfigurationLoader<?> loader();

  default void onReload() {}

  default void onSave() {}

  default void reload() {
    synchronized (this) {
      Configs.reload(this);
    }
    this.onReload();
  }

  default void save() {
    synchronized (this) {
      Configs.save(this);
    }
    this.onSave();
  }
}
