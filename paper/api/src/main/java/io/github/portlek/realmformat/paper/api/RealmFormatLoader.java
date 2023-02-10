package io.github.portlek.realmformat.paper.api;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableModule;

public interface RealmFormatLoader extends TerminableModule {
  byte[] load(@NotNull String worldName, boolean readOnly);

  void save(@NotNull String worldName, byte@NotNull[] serialized, boolean lock);

  void unlock(@NotNull String worldName);

  boolean locked(@NotNull String worldName);

  void delete(@NotNull String worldName);

  @NotNull
  List<String> list();

  boolean exists(@NotNull String worldName);
}
