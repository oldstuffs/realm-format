package io.github.portlek.realmformat.format.exception;

import lombok.experimental.StandardException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@StandardException
public final class CorruptedWorldException extends RealmException {

  private static final String MESSAGE = "World %s seems to be corrupted";

  public static void check(
    final boolean statement,
    @NotNull final String world,
    @Nullable final Throwable cause
  ) throws CorruptedWorldException {
    if (!statement) {
      throw new CorruptedWorldException(CorruptedWorldException.MESSAGE.formatted(world), cause);
    }
  }

  public static void check(final boolean statement, @NotNull final String world)
    throws CorruptedWorldException {
    CorruptedWorldException.check(statement, world, null);
  }
}
