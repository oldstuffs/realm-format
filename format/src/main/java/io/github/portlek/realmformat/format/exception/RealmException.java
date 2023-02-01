package io.github.portlek.realmformat.format.exception;

import lombok.AccessLevel;
import lombok.experimental.StandardException;

@StandardException(access = AccessLevel.PROTECTED)
abstract class RealmException extends Exception {}
