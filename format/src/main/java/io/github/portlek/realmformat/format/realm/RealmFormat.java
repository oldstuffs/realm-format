package io.github.portlek.realmformat.format.realm;

/**
 * An interface that contains common constants for Realm format.
 */
public interface RealmFormat {
  /**
   * The file extension of realm files.
   */
  String EXTENSION = ".realm";

  /**
   * Header of the realm file.
   */
  byte[] HEADER = new byte[] { -79, 11 };

  /**
   * The latest realm file version.
   */
  byte VERSION = 1;
}
