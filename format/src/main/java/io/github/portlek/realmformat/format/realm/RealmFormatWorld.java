package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.shiruka.nbt.CompoundTag;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface RealmFormatWorld {
  /**
   * Gets world's chunks.
   *
   * @return Chunks of the world.
   *
   * @since 1
   */
  @NotNull
  Map<RealmFormatChunkPosition, RealmFormatChunk> chunks();

  /**
   * Gets extra data of the world.
   *
   * @return Extra data of the world.
   */
  @NotNull
  CompoundTag extra();

  /**
   * Gets properties of the world.
   *
   * @return Properties of the world.
   */
  @NotNull
  RealmPropertyMap properties();

  /**
   * Gets world version.
   *
   * @return Realm's world version.
   *
   * @since 1
   */
  byte version();

  /**
   * Gets Minecraft world version.
   * <p>
   * This does NOT represent RealmFormat version.
   *
   * @return Minecraft world version.
   *
   * @since 1
   */
  byte worldVersion();
}
