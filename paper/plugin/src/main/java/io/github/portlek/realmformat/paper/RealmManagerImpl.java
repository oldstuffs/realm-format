package io.github.portlek.realmformat.paper;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.exception.CorruptedWorldException;
import io.github.portlek.realmformat.format.exception.InvalidWorldException;
import io.github.portlek.realmformat.format.exception.NewerFormatException;
import io.github.portlek.realmformat.format.exception.UnknownWorldException;
import io.github.portlek.realmformat.format.exception.WorldAlreadyExistsException;
import io.github.portlek.realmformat.format.exception.WorldInUseException;
import io.github.portlek.realmformat.format.exception.WorldLoadedException;
import io.github.portlek.realmformat.format.exception.WorldTooBigException;
import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.portlek.realmformat.paper.api.RealmManager;
import io.github.portlek.realmformat.paper.api.event.PostGenerateRealmWorldEvent;
import io.github.portlek.realmformat.paper.api.event.PreGenerateRealmWorldEvent;
import io.github.portlek.realmformat.paper.file.RealmConfig;
import io.github.portlek.realmformat.paper.loader.FileLoader;
import io.github.portlek.realmformat.paper.loader.MongoLoader;
import io.github.portlek.realmformat.paper.loader.UpdatableLoader;
import io.github.portlek.realmformat.paper.misc.Services;
import io.github.portlek.realmformat.paper.nms.RealmNmsBackend;
import io.github.shiruka.nbt.Tag;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tr.com.infumia.task.Promise;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

@Log4j2
final class RealmManagerImpl implements RealmManager, TerminableModule {

  private final Map<String, RealmWorld> loadedWorlds = new ConcurrentHashMap<>();

  private final Map<String, RealmLoader> loaders = new ConcurrentHashMap<>();

  private final RealmNmsBackend nms = Services.load(RealmNmsBackend.class);

  @NotNull
  @Override
  public Promise<@Nullable RealmWorld> asyncCreateEmptyWorld(
    @NotNull final RealmLoader loader,
    @NotNull final String worldName,
    final boolean readOnly,
    @NotNull final RealmPropertyMap propertyMap
  ) {
    return null;
  }

  @NotNull
  @Override
  public Promise<@Nullable RealmWorld> asyncGetWorld(@NotNull final String worldName) {
    return null;
  }

  @NotNull
  @Override
  public Promise<?> asyncImportWorld(
    @NotNull final File worldDir,
    @NotNull final String worldName,
    @NotNull final RealmLoader loader
  ) {
    return null;
  }

  @NotNull
  @Override
  public Promise<@Nullable RealmWorld> asyncLoadWorld(
    @NotNull final RealmLoader loader,
    @NotNull final String worldName,
    final boolean readOnly,
    @NotNull final RealmPropertyMap propertyMap
  ) {
    return null;
  }

  @NotNull
  @Override
  public Promise<Void> asyncMigrateWorld(
    @NotNull final String worldName,
    @NotNull final RealmLoader currentLoader,
    @NotNull final RealmLoader newLoader
  ) {
    return null;
  }

  @NotNull
  @Override
  public RealmWorld createEmptyWorld(
    @NotNull final RealmLoader loader,
    @NotNull final String worldName,
    final boolean readOnly,
    @NotNull final RealmPropertyMap propertyMap
  ) throws WorldAlreadyExistsException, IOException {
    WorldAlreadyExistsException.check(!loader.worldExists(worldName), worldName);
    RealmManagerImpl.log.info("Creating empty world " + worldName + ".");
    final var start = System.currentTimeMillis();
    final var world =
      this.nms.createRealmWorld(
          loader,
          worldName,
          new Long2ObjectOpenHashMap<>(),
          Tag.createCompound(),
          Tag.createList(),
          this.nms.worldVersion(),
          propertyMap,
          readOnly,
          !readOnly,
          new Long2ObjectOpenHashMap<>()
        );
    loader.saveWorld(worldName, world.serialize(), !readOnly);
    RealmManagerImpl.log.info(
      "World " + worldName + " created in " + (System.currentTimeMillis() - start) + "ms."
    );
    this.registerWorld(world);
    return world;
  }

  @Override
  public void generateWorld(@NotNull final RealmWorld world) {
    Preconditions.checkArgument(
      world.readOnly() || world.locked(),
      "This world cannot be loaded, as it has not been locked."
    );
    final var preEvent = new PreGenerateRealmWorldEvent(world);
    preEvent.callEvent();
    if (preEvent.isCancelled()) {
      return;
    }
    this.nms.generateWorld(preEvent.world());
    new PostGenerateRealmWorldEvent(preEvent.world()).callEvent();
  }

  @NotNull
  @Override
  public RealmWorld getWorld(@NotNull final String worldName) {
    return null;
  }

  @Override
  public void importWorld(
    @NotNull final File worldDir,
    @NotNull final String worldName,
    @NotNull final RealmLoader loader
  )
    throws WorldAlreadyExistsException, InvalidWorldException, WorldLoadedException, WorldTooBigException, IOException {}

  @NotNull
  @Override
  public RealmWorld loadWorld(
    @NotNull final RealmLoader loader,
    @NotNull final String worldName,
    final boolean readOnly,
    @NotNull final RealmPropertyMap propertyMap
  )
    throws UnknownWorldException, IOException, CorruptedWorldException, NewerFormatException, WorldInUseException {
    return null;
  }

  @NotNull
  @Override
  public List<RealmWorld> loadedWorlds() {
    return null;
  }

  @Nullable
  @Override
  public RealmLoader loader(@NotNull final String dataSource) {
    return this.loaders.get(dataSource);
  }

  @Override
  public void migrateWorld(
    @NotNull final String worldName,
    @NotNull final RealmLoader currentLoader,
    @NotNull final RealmLoader newLoader
  ) throws IOException, WorldInUseException, WorldAlreadyExistsException, UnknownWorldException {}

  @Override
  public void registerLoader(@NotNull final String dataSource, @NotNull final RealmLoader loader) {
    Preconditions.checkArgument(
      !this.loaders.containsKey(dataSource),
      "Data source %s already has a declared loader!",
      dataSource
    );
    if (loader instanceof UpdatableLoader updatableLoader) {
      try {
        updatableLoader.update();
      } catch (final UpdatableLoader.NewerDatabaseException e) {
        RealmManagerImpl.log.error(
          "Data source " +
          dataSource +
          " version is " +
          e.databaseVersion() +
          ", while" +
          " this SWM version only supports up to version " +
          e.currentVersion() +
          "."
        );
        return;
      } catch (final IOException ex) {
        RealmManagerImpl.log.error("Failed to check if data source " + dataSource + " is updated:");
        ex.printStackTrace();
        return;
      }
    }
    this.loaders.put(dataSource, loader);
  }

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {
    final var config = Services.load(RealmConfig.class);
    this.registerLoader("file", new FileLoader(config.fileLoaderPath().toFile()));
    if (config.mongo().enabled()) {
      this.registerLoader("mongo", new MongoLoader(config.mongo()));
    }
  }

  private void registerWorld(@NotNull final RealmWorld world) {
    this.loadedWorlds.put(world.name(), world);
  }
}
