package io.github.portlek.realmformat.paper.api;

import io.github.portlek.realmformat.format.property.RealmFormatProperties;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormat;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import java.io.File;
import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * An interface that manages the general logic of the realm format.
 */
public interface RealmFormatManager {
  /**
   * Creates an empty world and saves it using a specified {@link RealmFormatLoader}.
   * <p>
   * Use the {@link #generateWorld(RealmFormatWorld)} method to add the loaded world to the server's world list.
   *
   * @param loader The loader used to store the world.
   * @param worldName The world name to create.
   * @param readOnly The read-only flag indicates whether read-only mode is enabled or not.
   * @param properties The properties that contain all the properties of the world.
   *
   * @return A {@link RealmFormatWorld} that's the in-memory representation of the world.
   */
  @NotNull
  Optional<RealmFormatWorld> createEmptyWorld(
    @NotNull RealmFormatLoader loader,
    @NotNull String worldName,
    boolean readOnly,
    @NotNull RealmFormatPropertyMap properties
  );

  /**
   * Generates a Minecraft world from a {@link RealmFormatWorld} and adds it to the server's world list.
   *
   * @param world The world to be added to the server's world list.
   */
  void generateWorld(@NotNull RealmFormatWorld world);

  /**
   * Imports an anvil world into the {@link RealmFormat} and saves it using the specified loader.
   *
   * @param worldDirectory The anvil world directory where the world is.
   * @param worldName The world name of the newly imported world into the loader.
   * @param loader The loader that's gonna be used to store the world.
   */
  void importAnvilWorld(
    @NotNull File worldDirectory,
    @NotNull String worldName,
    @NotNull RealmFormatLoader loader
  );

  /**
   * Loads a world using a specified {@link RealmFormatLoader}.
   * <p>
   * Use {@link #generateWorld(RealmFormatWorld)} method to add the loaded world to the server's world list.
   *
   * @param loader The loader used to retrieve the world.
   * @param worldName The world name to load.
   * @param readOnly The read-only flag indicates whether read-only mode is enabled or not.
   * @param properties The properties that contain all the properties of the world.
   *
   * @return A {@link RealmFormatWorld} that's the in-memory representation of the world.
   *
   * @see RealmFormatProperties
   */
  @NotNull
  Optional<RealmFormatWorld> loadWorld(
    @NotNull RealmFormatLoader loader,
    @NotNull String worldName,
    boolean readOnly,
    @NotNull RealmFormatPropertyMap properties
  );

  /**
   * Obtains the loaded {@link RealmFormatWorld}s.
   *
   * @return A collection of {@link RealmFormatWorld} that's loaded.
   */
  @NotNull
  @UnmodifiableView
  Collection<RealmFormatWorld> loadedWorlds();

  /**
   * Retrieves the {@link RealmFormatLoader} using the specified type.
   *
   * @param type The type to retrieve the {@link RealmFormatLoader}.
   *
   * @return A {@link RealmFormatLoader} instance.
   */
  @NotNull
  Optional<RealmFormatLoader> loader(@NotNull String type);

  /**
   * Attempts to migrate the world using to retrieve the old loader, then saves it using the new loader.
   *
   * @param worldName The world name to retrieve then save.
   * @param oldLoader The old loader to retrieve the world.
   * @param newLoader The new loader to save the world.
   */
  void migrateWorld(
    @NotNull String worldName,
    @NotNull RealmFormatLoader oldLoader,
    @NotNull RealmFormatLoader newLoader
  );

  /**
   * Registers a {@link RealmFormatLoader} to the loader list for future use.
   *
   * @param type The type of the loader.
   * @param loader The loader to register.
   */
  void registerLoader(@NotNull String type, @NotNull RealmFormatLoader loader);

  /**
   * Retrieves the {@link RealmFormatWorld} using the specified world name.
   *
   * @param worldName The world name to retrieve {@link RealmFormatWorld} instance.
   *
   * @return A {@link RealmFormatWorld} instance by the world name.
   */
  @NotNull
  Optional<RealmFormatWorld> world(@NotNull String worldName);
}
