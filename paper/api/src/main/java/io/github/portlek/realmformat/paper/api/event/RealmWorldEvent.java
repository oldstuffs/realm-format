package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.realm.RealmWorld;
import lombok.Getter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class RealmWorldEvent extends Event {

  @Getter
  @NotNull
  protected RealmWorld world;

  protected RealmWorldEvent(final boolean isAsync, @NotNull final RealmWorld world) {
    super(isAsync);
    this.world = world;
  }
}
