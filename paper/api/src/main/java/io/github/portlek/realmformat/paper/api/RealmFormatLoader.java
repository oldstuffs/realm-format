package io.github.portlek.realmformat.paper.api;

import io.github.portlek.realmformat.format.realm.RealmFormat;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableModule;

/**
 * An interface that represents loader for {@link RealmFormat} worlds.
 */
public interface RealmFormatLoader extends TerminableModule {
  /**
   * Deletes the world.
   *
   * @param worldName The world to delete.
   */
  void delete(@NotNull String worldName);

  /**
   * Whether the world exists or not.
   *
   * @param worldName The world name to check.
   *
   * @return {@code true} if the world exist.
   */
  boolean exists(@NotNull String worldName);

  /**
   * Returns list of the worlds.
   *
   * @return World list.
   */
  @NotNull
  List<String> list();

  /**
   * Tries to load the world.
   *
   * @param worldName The world name to load.
   * @param readOnly The read only to reload.
   *
   * @return Loaded world.
   */
  byte[] load(@NotNull String worldName, boolean readOnly);

  /**
   * Whether the world locked or not.
   *
   * @param worldName The world name to check.
   *
   * @return {@code true} if the world is locked.
   */
  boolean locked(@NotNull String worldName);

  /**
   * Saves the world.
   *
   * @param worldName The world name to save.
   * @param serialized The serialized world to save.
   * @param lock The lock to save.
   */
  void save(@NotNull String worldName, byte@NotNull[] serialized, boolean lock);

  /**
   * Unlocks the world.
   *
   * @param worldName The world name to unlock.
   */
  void unlock(@NotNull String worldName);
}
