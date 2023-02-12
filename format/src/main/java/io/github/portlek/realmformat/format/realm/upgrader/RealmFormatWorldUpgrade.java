package io.github.portlek.realmformat.format.realm.upgrader;

import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RealmFormatWorldUpgrade extends UnaryOperator<@NotNull RealmFormatWorld> {}
