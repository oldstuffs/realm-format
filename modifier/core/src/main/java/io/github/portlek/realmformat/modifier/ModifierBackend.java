package io.github.portlek.realmformat.modifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModifierBackend {
  boolean flushEntities(@NotNull Object storage);

  @Nullable
  Object chunkAt(@NotNull Object world, int x, int z);

  @NotNull
  Object chunkTask(
    @NotNull Object scheduler,
    @NotNull Object world,
    int chunkX,
    int chunkZ,
    @NotNull Object chunkHolder,
    @NotNull Object priority,
    @NotNull Object status
  );

  @NotNull
  Object injectCustomWorlds();

  boolean isCustomWorld(@NotNull Object world);

  @Nullable
  Object loadEntities(@NotNull Object storage, @NotNull Object chunkCoordinates);

  boolean saveChunk(@NotNull Object world, @NotNull Object chunkAccess);

  boolean storeEntities(@NotNull Object storage, @NotNull Object entities);
}
