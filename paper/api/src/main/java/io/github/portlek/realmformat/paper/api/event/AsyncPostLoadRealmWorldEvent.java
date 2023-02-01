package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.realm.RealmWorld;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPostLoadRealmWorldEvent extends RealmWorldEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public AsyncPostLoadRealmWorldEvent(@NotNull final RealmWorld world) {
    super(true, world);
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return AsyncPostLoadRealmWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return AsyncPostLoadRealmWorldEvent.getHandlerList();
  }
}
