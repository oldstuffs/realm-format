package io.github.portlek.realmformat.format.realm.upgrader.v1_11;

import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrade;
import org.jetbrains.annotations.NotNull;

public final class RealmFormatWorldUpgradeV1_11 implements RealmFormatWorldUpgrade {

  @NotNull
  @Override
  public RealmFormatWorld apply(@NotNull final RealmFormatWorld t) {
    return t;
  }
}
