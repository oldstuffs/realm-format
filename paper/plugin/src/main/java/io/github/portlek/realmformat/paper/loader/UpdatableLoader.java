package io.github.portlek.realmformat.paper.loader;

import io.github.portlek.realmformat.format.loader.RealmLoader;
import java.io.IOException;
import lombok.Getter;

public abstract class UpdatableLoader implements RealmLoader {

  public abstract void update() throws NewerDatabaseException, IOException;

  @Getter
  public static final class NewerDatabaseException extends Exception {

    private final int currentVersion;

    private final int databaseVersion;

    public NewerDatabaseException(final int currentVersion, final int databaseVersion) {
      this.currentVersion = currentVersion;
      this.databaseVersion = databaseVersion;
    }
  }
}
