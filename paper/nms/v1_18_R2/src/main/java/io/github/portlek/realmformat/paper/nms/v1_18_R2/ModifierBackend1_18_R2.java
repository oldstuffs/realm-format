package io.github.portlek.realmformat.paper.nms.v1_18_R2;

import io.github.portlek.realmformat.modifier.ModifierBackend;
import org.jetbrains.annotations.NotNull;

public final class ModifierBackend1_18_R2 implements ModifierBackend {

  @NotNull
  @Override
  public Object chunkAt(@NotNull final Object world, final int x, final int z) {
    return null;
  }

  @NotNull
  @Override
  public Object chunkTask(
    @NotNull final Object scheduler,
    @NotNull final Object world,
    final int chunkX,
    final int chunkZ,
    @NotNull final Object chunkHolder,
    @NotNull final Object priority,
    @NotNull final Object status
  ) {
    return null;
  }

  @Override
  public boolean flushEntities(@NotNull final Object storage) {
    return false;
  }

  @NotNull
  @Override
  public Object injectCustomWorlds() {
    return null;
  }

  @Override
  public boolean isCustomWorld(@NotNull final Object world) {
    return false;
  }

  @NotNull
  @Override
  public Object loadEntities(
    @NotNull final Object storage,
    @NotNull final Object chunkCoordinates
  ) {
    return null;
  }

  @Override
  public boolean saveChunk(@NotNull final Object world, @NotNull final Object chunkAccess) {
    return false;
  }

  @Override
  public boolean storeEntities(@NotNull final Object storage, @NotNull final Object entities) {
    return false;
  }
}
