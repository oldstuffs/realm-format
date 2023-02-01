package io.github.portlek.realmformat.format.exception;

import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

@StandardException
public final class InvalidVersionException extends RealmFormatException {

  private static final String MESSAGE = "RealmFormat does not support Minecraft %s!";

  public static void check(final boolean statement, @NotNull final String version)
    throws InvalidVersionException {
    if (!statement) {
      throw new InvalidVersionException(InvalidVersionException.MESSAGE.formatted(version));
    }
  }
}
