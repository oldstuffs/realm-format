package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.old.realm.RealmWorld;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPostCreateEmptyRealmWorldEvent extends RealmWorldEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public AsyncPostCreateEmptyRealmWorldEvent(@NotNull final RealmWorld world) {
    super(true, world);
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return AsyncPostCreateEmptyRealmWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return AsyncPostCreateEmptyRealmWorldEvent.getHandlerList();
  }
}
