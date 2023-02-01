package io.github.portlek.realmformat.format.exception;

import java.io.File;
import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;

@StandardException
public final class InvalidWorldException extends RealmException {

  private static final String MESSAGE = "Directory %s does not contain a valid MC world!%s";

  public static void check(
    final boolean statement,
    @NotNull final File worldDirectory,
    @NotNull final String reason
  ) throws InvalidWorldException {
    if (!statement) {
      throw new InvalidWorldException(
        InvalidWorldException.MESSAGE.formatted(worldDirectory.getPath(), reason)
      );
    }
  }

  public static void check(final boolean statement, @NotNull final File worldDirectory)
    throws InvalidWorldException {
    InvalidWorldException.check(statement, worldDirectory, "");
  }
}
