package io.github.portlek.realmformat.paper.upgrader;

import io.github.portlek.realmformat.format.old.realm.RealmWorld;
import org.jetbrains.annotations.NotNull;

public interface Upgrade {
  void upgrade(@NotNull RealmWorld world);
}
