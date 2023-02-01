package io.github.portlek.realmformat.format.exception;

import lombok.experimental.StandardException;

@StandardException
public final class NewerFormatException extends RealmException {

  private static final String MESSAGE = "v%s";

  public static void check(final boolean statement, final byte version)
    throws NewerFormatException {
    if (!statement) {
      throw new NewerFormatException(NewerFormatException.MESSAGE.formatted(version));
    }
  }
}
