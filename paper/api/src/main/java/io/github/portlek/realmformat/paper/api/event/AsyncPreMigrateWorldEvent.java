package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.loader.RealmLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPreMigrateWorldEvent extends Event implements CancellableEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  @Getter
  private final AtomicBoolean cancelled = new AtomicBoolean();

  @Getter
  @Setter
  @NotNull
  private RealmLoader currentLoader;

  @Getter
  @Setter
  @NotNull
  private RealmLoader newLoader;

  @Getter
  @Setter
  @NotNull
  private String worldName;

  public AsyncPreMigrateWorldEvent(
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
    return AsyncPreMigrateWorldEvent.HANDLER_LIST;
  }

  @Override
  @NotNull
  public HandlerList getHandlers() {
    return AsyncPreMigrateWorldEvent.getHandlerList();
  }
}
