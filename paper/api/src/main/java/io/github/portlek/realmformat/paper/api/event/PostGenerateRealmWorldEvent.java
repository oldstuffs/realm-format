package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.realm.RealmWorld;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PostGenerateRealmWorldEvent extends RealmWorldEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public PostGenerateRealmWorldEvent(@NotNull final RealmWorld world) {
    super(false, world);
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return PostGenerateRealmWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return PostGenerateRealmWorldEvent.getHandlerList();
  }
}
