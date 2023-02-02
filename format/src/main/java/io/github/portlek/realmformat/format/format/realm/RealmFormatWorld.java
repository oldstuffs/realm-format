package io.github.portlek.realmformat.format.format.realm;

public interface RealmFormatWorld {
  /**
   * Gets world version.
   *
   * @return RealmFormat's world version.
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
