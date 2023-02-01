package io.github.portlek.realmformat.format.exception;

import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

@StandardException
public final class WorldAlreadyExistsException extends RealmFormatException {

  private static final String MESSAGE = "World %s already exists!";

  public static void check(final boolean statement, @NotNull final String world)
    throws WorldAlreadyExistsException {
    if (!statement) {
      throw new WorldAlreadyExistsException(WorldAlreadyExistsException.MESSAGE.formatted(world));
    }
  }
}
