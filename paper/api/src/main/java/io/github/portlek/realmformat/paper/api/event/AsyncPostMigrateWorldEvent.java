package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.loader.RealmLoader;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPostMigrateWorldEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  @Getter
  @NotNull
  private final RealmLoader currentLoader;

  @Getter
  @NotNull
  private final RealmLoader newLoader;

  @Getter
  @NotNull
  private final String worldName;

  public AsyncPostMigrateWorldEvent(
    @NotNull final String worldName,
    @NotNull final RealmLoader currentLoader,
    @NotNull final RealmLoader newLoader
  ) {
    super(true);
    this.worldName = worldName;
    this.currentLoader = currentLoader;
    this.newLoader = newLoader;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return AsyncPostMigrateWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return AsyncPostMigrateWorldEvent.getHandlerList();
  }
}
