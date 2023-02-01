package io.github.portlek.realmformat.format.exception;

import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

@StandardException
public final class WorldTooBigException extends RealmException {

  private static final String MESSAGE = "World %s is too big to be converted into the SRF!";

  public static void check(final boolean statement, @NotNull final String world)
    throws WorldTooBigException {
    if (!statement) {
      throw new WorldTooBigException(WorldTooBigException.MESSAGE.formatted(world));
    }
  }
}
