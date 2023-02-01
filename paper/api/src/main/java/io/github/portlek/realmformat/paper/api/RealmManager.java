package io.github.portlek.realmformat.paper.api;

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
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tr.com.infumia.task.Promise;

public interface RealmManager {
  @NotNull
  Promise<@Nullable RealmWorld> asyncCreateEmptyWorld(
    @NotNull RealmLoader loader,
    @NotNull String worldName,
    boolean readOnly,
    @NotNull RealmPropertyMap propertyMap
  );

  @NotNull
  Promise<@Nullable RealmWorld> asyncGetWorld(@NotNull String worldName);

  @NotNull
  Promise<?> asyncImportWorld(
    @NotNull File worldDir,
    @NotNull String worldName,
    @NotNull RealmLoader loader
  );

  @NotNull
  Promise<@Nullable RealmWorld> asyncLoadWorld(
    @NotNull RealmLoader loader,
    @NotNull String worldName,
    boolean readOnly,
    @NotNull RealmPropertyMap propertyMap
  );

  @NotNull
  Promise<Void> asyncMigrateWorld(
    @NotNull String worldName,
    @NotNull RealmLoader currentLoader,
    @NotNull RealmLoader newLoader
  );

  @NotNull
  RealmWorld createEmptyWorld(
    @NotNull RealmLoader loader,
    @NotNull String worldName,
    boolean readOnly,
    @NotNull RealmPropertyMap propertyMap
  ) throws WorldAlreadyExistsException, IOException;

  void generateWorld(@NotNull RealmWorld world);

  @NotNull
  RealmWorld getWorld(@NotNull String worldName);

  void importWorld(@NotNull File worldDir, @NotNull String worldName, @NotNull RealmLoader loader)
    throws WorldAlreadyExistsException, InvalidWorldException, WorldLoadedException, WorldTooBigException, IOException;

  @NotNull
  RealmWorld loadWorld(
    @NotNull RealmLoader loader,
    @NotNull String worldName,
    boolean readOnly,
    @NotNull RealmPropertyMap propertyMap
  )
    throws UnknownWorldException, IOException, CorruptedWorldException, NewerFormatException, WorldInUseException;

  @NotNull
  List<RealmWorld> loadedWorlds();

  @Nullable
  RealmLoader loader(@NotNull String dataSource);

  void migrateWorld(
    @NotNull String worldName,
    @NotNull RealmLoader currentLoader,
    @NotNull RealmLoader newLoader
  ) throws IOException, WorldInUseException, WorldAlreadyExistsException, UnknownWorldException;

  void registerLoader(@NotNull String dataSource, @NotNull RealmLoader loader);
}
