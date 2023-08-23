package io.github.portlek.realmformat.format.misc;

public interface FailableSupplier<X, T extends Throwable> {
    X get() throws T;
}
