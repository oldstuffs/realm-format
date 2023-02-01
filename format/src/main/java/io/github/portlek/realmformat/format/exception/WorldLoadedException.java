package io.github.portlek.realmformat.format.exception;

import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

@StandardException
public final class WorldLoadedException extends RealmFormatException {

  private static final String MESSAGE = "World %s is loaded! Unload it before importing it.";

  public static void check(final boolean statement, @NotNull final String world)
    throws WorldLoadedException {
    if (!statement) {
      throw new WorldLoadedException(WorldLoadedException.MESSAGE.formatted(world));
    }
  }
}
