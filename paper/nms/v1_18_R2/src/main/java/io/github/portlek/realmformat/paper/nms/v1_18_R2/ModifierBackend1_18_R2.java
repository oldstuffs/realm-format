package io.github.portlek.realmformat.paper.nms.v1_18_R2;

import com.mojang.datafixers.util.Either;
import io.github.portlek.realmformat.modifier.Modifier;
import io.github.portlek.realmformat.modifier.ModifierBackend;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.entity.ChunkEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public final class ModifierBackend1_18_R2 implements ModifierBackend {

  @NotNull
  private final RealmNmsBackend1_18_R2 nms;

  private ModifierBackend1_18_R2(@NotNull final RealmNmsBackend1_18_R2 nms) {
    this.nms = nms;
  }

  static void initialize(@NotNull final RealmNmsBackend1_18_R2 instance) {
    Modifier.initiateBackend(new ModifierBackend1_18_R2(instance));
  }

  @NotNull
  @Override
  public Object chunkAt(@NotNull final Object world, final int x, final int z) {
    final var w = (RealmWorldServer) world;
    return Either.left(w.imposterChunk(x, z));
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
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean flushEntities(@NotNull final Object storage) {
    final EntityStorage entityStorage = (EntityStorage) storage;
    return this.isCustomWorld(entityStorage.level);
  }

  @Nullable
  @Override
  public Object injectCustomWorlds() {
    return this.nms.injectDefaultWorlds();
  }

  @Override
  public boolean isCustomWorld(@NotNull final Object world) {
    if (world instanceof RealmWorldServer) {
      return true;
    } else if (world instanceof Level) {
      return false;
    } else {
      throw new IllegalStateException(
        "World is probably not a world, was given %s. Check the classmodifier to ensure the correct level field is passed (check for field name changes)".formatted(
            world
          )
      );
    }
  }

  @Nullable
  @Override
  public Object loadEntities(
    @NotNull final Object storage,
    @NotNull final Object chunkCoordinates
  ) {
    final EntityStorage entityStorage = (EntityStorage) storage;
    if (!this.isCustomWorld(entityStorage.level)) {
      return null;
    }
    return ((RealmWorldServer) entityStorage.level).handleEntityLoad((ChunkPos) chunkCoordinates);
  }

  @Override
  public boolean saveChunk(@NotNull final Object world, @NotNull final Object chunkAccess) {
    if (!(world instanceof RealmWorldServer)) {
      return false;
    }
    if (
      !(chunkAccess instanceof ImposterProtoChunk || chunkAccess instanceof LevelChunk) ||
      !((ChunkAccess) chunkAccess).isUnsaved()
    ) {
      return true;
    }
    final LevelChunk chunk;
    if (chunkAccess instanceof ImposterProtoChunk) {
      chunk = ((ImposterProtoChunk) chunkAccess).getWrapped();
    } else {
      chunk = (LevelChunk) chunkAccess;
    }
    ((RealmWorldServer) world).saveChunk(chunk);
    chunk.setUnsaved(false);
    return true;
  }

  @Override
  public boolean storeEntities(@NotNull final Object storage, @NotNull final Object entities) {
    final var entityStorage = (EntityStorage) storage;
    if (!this.isCustomWorld(entityStorage.level)) {
      return false;
    }
    ((RealmWorldServer) entityStorage.level).handleEntityUnLoad((ChunkEntities<Entity>) entities);
    return true;
  }
}
