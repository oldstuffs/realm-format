package io.github.portlek.realmformat.paper.api.event;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPreGetWorldEvent extends Event implements CancellableEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  @Getter
  private final AtomicBoolean cancelled = new AtomicBoolean();

  @Getter
  @Setter
  @NotNull
  private String worldName;

  public AsyncPreGetWorldEvent(@NotNull final String worldName) {
    super(true);
    this.worldName = worldName;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return AsyncPreGetWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return AsyncPreGetWorldEvent.getHandlerList();
  }
}
