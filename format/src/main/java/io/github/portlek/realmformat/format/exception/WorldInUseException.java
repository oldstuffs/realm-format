package io.github.portlek.realmformat.format.exception;

import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

@StandardException
public final class WorldInUseException extends RealmException {

  public static void check(final boolean statement, @NotNull final String world)
    throws WorldInUseException {
    if (!statement) {
      throw new WorldInUseException(world);
    }
  }
}
