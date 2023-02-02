package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.old.realm.RealmWorld;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPostGetRealmWorldEvent extends RealmWorldEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public AsyncPostGetRealmWorldEvent(@NotNull final RealmWorld world) {
    super(true, world);
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return AsyncPostGetRealmWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return AsyncPostGetRealmWorldEvent.getHandlerList();
  }
}
