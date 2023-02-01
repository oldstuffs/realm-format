package io.github.portlek.realmformat.format.loader;

import io.github.portlek.realmformat.format.exception.UnknownWorldException;
import io.github.portlek.realmformat.format.exception.WorldInUseException;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface RealmLoader {
  void deleteWorld(@NotNull String worldName) throws UnknownWorldException, IOException;

  boolean isWorldLocked(@NotNull String worldName) throws UnknownWorldException, IOException;

  @NotNull
  List<String> listWorlds() throws IOException;

  byte@NotNull[] loadWorld(@NotNull String worldName, boolean readOnly)
    throws UnknownWorldException, WorldInUseException, IOException;

  void saveWorld(@NotNull String worldName, byte[] serializedWorld, boolean lock)
    throws IOException;

  void unlockWorld(@NotNull String worldName) throws UnknownWorldException, IOException;

  boolean worldExists(@NotNull String worldName) throws IOException;
}
