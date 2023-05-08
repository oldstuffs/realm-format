package io.github.portlek.realmformat.paper.nms.v1_19_R2;

import io.github.portlek.realmformat.modifier.ModifierBackend;
import io.github.portlek.realmformat.paper.nms.NmsBackend;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ModifierBackendV1_19_R2 implements ModifierBackend {

  @NotNull
  private final NmsBackend backend;

  public ModifierBackendV1_19_R2(@NotNull final NmsBackend backend) {
    this.backend = backend;
  }

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

  @Nullable
  @Override
  public Object injectCustomWorlds() {
    return null;
  }

  @Override
  public boolean isCustomWorld(@NotNull final Object world) {
    return false;
  }

  @Nullable
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
