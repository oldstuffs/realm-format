package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fires after the {@link RealmFormatLoader}s loaded.
 */
public final class RealmFormatLoaderLoadEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  /**
   * ctor.
   */
  public RealmFormatLoaderLoadEvent() {
    super(!Bukkit.isPrimaryThread());
  }

  /**
   * Returns handler list.
   *
   * @return Handler list.
   */
  @NotNull
  public static HandlerList getHandlerList() {
    return RealmFormatLoaderLoadEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return RealmFormatLoaderLoadEvent.getHandlerList();
  }
}
