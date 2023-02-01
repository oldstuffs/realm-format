package io.github.portlek.realmformat.format.exception;

import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

@StandardException
public final class UnknownWorldException extends RealmFormatException {

  private static final String MESSAGE = "Unknown world %s";

  public static void check(final boolean statement, @NotNull final String world)
    throws UnknownWorldException {
    if (!statement) {
      throw new UnknownWorldException(UnknownWorldException.MESSAGE.formatted(world));
    }
  }
}
