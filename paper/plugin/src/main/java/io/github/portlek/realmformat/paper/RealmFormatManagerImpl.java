package io.github.portlek.realmformat.paper;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatSerializers;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrades;
import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.api.RealmFormatManager;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import io.github.portlek.realmformat.paper.loader.RealmFormatLoaderMap;
import io.github.portlek.realmformat.paper.nms.NmsBackend;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

@Log4j2
final class RealmFormatManagerImpl implements RealmFormatManager {

  private final NmsBackend backend = Services.load(NmsBackend.class);

  private final Map<String, RealmFormatWorld> loadedWorlds = new ConcurrentHashMap<>();

  private final RealmFormatLoaderMap loaderMap = Services.load(RealmFormatLoaderMap.class);

  @NotNull
  @Override
  public Optional<RealmFormatWorld> createEmptyWorld(
    @NotNull final RealmFormatLoader loader,
    @NotNull final String worldName,
    final boolean readOnly,
    @NotNull final RealmFormatPropertyMap properties
  ) {
    return Optional.empty();
  }

  @Override
  public void generateWorld(@NotNull final RealmFormatWorld world) {}

  @Override
  public void importAnvilWorld(
    @NotNull final File worldDirectory,
    @NotNull final String worldName,
    @NotNull final RealmFormatLoader loader
  ) {}

  @NotNull
  @Override
  public Optional<RealmFormatWorld> loadWorld(
    @NotNull final RealmFormatLoader loader,
    @NotNull final String worldName,
    final boolean readOnly,
    @NotNull final RealmFormatPropertyMap properties
  ) {
    final var start = System.currentTimeMillis();
    RealmFormatManagerImpl.log.info("Loading world {}.", worldName);
    final var serializedWorld = loader.load(worldName, readOnly);
    final RealmFormatWorld world;
    try {
      world = RealmFormatSerializers.deserialize(serializedWorld, properties);
      Preconditions.checkState(
        world.worldVersion() <= this.backend.worldVersion(),
        "World's world version: %s, Server's world version: %s"
      );
      if (world.worldVersion() < this.backend.worldVersion()) {
        RealmFormatWorldUpgrades.apply(world, this.backend.worldVersion());
      }
    } catch (final Exception e) {
      if (!readOnly) {
        loader.unlock(worldName);
      }
      throw e;
    }
    RealmFormatManagerImpl.log.info(
      "World {} loaded in {}ms.",
      worldName,
      System.currentTimeMillis() - start
    );
    this.loadedWorlds.put(worldName, world);
    return Optional.of(world);
  }

  @NotNull
  @Override
  @UnmodifiableView
  public Collection<RealmFormatWorld> loadedWorlds() {
    return Collections.unmodifiableCollection(this.loadedWorlds.values());
  }

  @NotNull
  @Override
  public Optional<RealmFormatLoader> loader(@NotNull final String type) {
    return Optional.empty();
  }

  @Override
  public void migrateWorld(
    @NotNull final String worldName,
    @NotNull final RealmFormatLoader oldLoader,
    @NotNull final RealmFormatLoader newLoader
  ) {}

  @Override
  public void registerLoader(@NotNull final String type, @NotNull final RealmFormatLoader loader) {}

  @NotNull
  @Override
  public Optional<RealmFormatWorld> world(@NotNull final String worldName) {
    return Optional.empty();
  }
}
