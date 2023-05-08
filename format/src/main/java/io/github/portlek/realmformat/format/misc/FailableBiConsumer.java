package io.github.portlek.realmformat.format.misc;

public interface FailableBiConsumer<X, Y, T extends Throwable> {
  void accept(X x, Y y) throws T;
}
