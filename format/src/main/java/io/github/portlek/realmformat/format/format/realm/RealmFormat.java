package io.github.portlek.realmformat.format.format.realm;

/**
 * An interface that contains common constants for RealmFormat.
 */
public interface RealmFormat {
  /**
   * Header of the realm file.
   */
  byte[] HEADER = new byte[] { -79, 11 };

  /**
   * The latest realm file version.
   */
  byte VERSION = 1;
}
