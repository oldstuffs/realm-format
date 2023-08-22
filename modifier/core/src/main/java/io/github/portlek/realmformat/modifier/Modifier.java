package io.github.portlek.realmformat.modifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class Modifier {

  private final AtomicReference<ModifierBackend> BACKEND = new AtomicReference<>();

  @Nullable
  public CompletableFuture<?> chunkAt(@NotNull final Object world, final int x, final int z) {
    final ModifierBackend backend = Modifier.backend();
    if (backend == null || !Modifier.isCustomWorld(world)) {
      return null;
    }
    return CompletableFuture.supplyAsync(() -> backend.chunkAt(world, x, z));
  }

  @Nullable
  public Object chunkTask(
    @NotNull final Object scheduler,
    @NotNull final Object world,
    final int chunkX,
    final int chunkZ,
    @NotNull final Object chunkHolder,
    @NotNull final Object priority,
    @NotNull final Object status
  ) {
    final ModifierBackend backend = Modifier.backend();
    if (backend == null || !Modifier.isCustomWorld(world)) {
      return null;
    }
    return backend.chunkTask(scheduler, world, chunkX, chunkZ, chunkHolder, priority, status);
  }

  public boolean flushEntities(final Object storage) {
    final ModifierBackend backend = Modifier.backend();
    if (backend == null) {
      return false;
    }
    return backend.flushEntities(storage);
  }

  public void initiateBackend(@NotNull final ModifierBackend backend) {
    if (!Modifier.BACKEND.compareAndSet(null, backend)) {
      throw new RuntimeException("You cannot initiate the backend twice!");
    }
  }

  @Nullable
  public Object injectCustomWorlds() {
    final ModifierBackend backend = Modifier.backend();
    if (backend == null) {
      return null;
    }
    return backend.injectCustomWorlds();
  }

  public boolean isCustomWorld(final Object world) {
    final ModifierBackend backend = Modifier.backend();
    return backend != null && backend.isCustomWorld(world);
  }

  @Nullable
  public Object loadEntities(
    @NotNull final Object storage,
    @NotNull final Object chunkCoordinates
  ) {
    final ModifierBackend backend = Modifier.backend();
    if (backend == null) {
      return null;
    }
    return backend.loadEntities(storage, chunkCoordinates);
  }

  public boolean saveChunk(@NotNull final Object world, @NotNull final Object chunkAccess) {
    final ModifierBackend backend = Modifier.backend();
    return backend != null && backend.saveChunk(world, chunkAccess);
  }

  public boolean storeEntities(@NotNull final Object storage, @NotNull final Object entities) {
    final ModifierBackend backend = Modifier.backend();
    if (backend == null) {
      return false;
    }
    return backend.storeEntities(storage, entities);
  }

  @Nullable
  private ModifierBackend backend() {
    return Modifier.BACKEND.get();
  }
}
