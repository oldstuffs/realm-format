package io.github.portlek.realmformat.format.realm.upgrader.v1_14;

import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrade;
import org.jetbrains.annotations.NotNull;

public final class RealmFormatWorldUpgradeV1_14 implements RealmFormatWorldUpgrade {

  @NotNull
  @Override
  public RealmFormatWorld apply(@NotNull final RealmFormatWorld t) {
    return t;
  }
}
