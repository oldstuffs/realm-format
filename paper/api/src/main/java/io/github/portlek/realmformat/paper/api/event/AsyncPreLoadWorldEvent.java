package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPreLoadWorldEvent extends Event implements CancellableEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  @Getter
  private final AtomicBoolean cancelled = new AtomicBoolean();

  @Getter
  @Setter
  @NotNull
  private RealmLoader loader;

  @Getter
  @Setter
  @NotNull
  private RealmPropertyMap propertyMap;

  @Getter
  @Setter
  private boolean readOnly;

  @Getter
  @Setter
  @NotNull
  private String worldName;

  public AsyncPreLoadWorldEvent(
    @NotNull final RealmLoader loader,
    @NotNull final String worldName,
    final boolean readOnly,
    @NotNull final RealmPropertyMap propertyMap
  ) {
    super(true);
    this.loader = loader;
    this.worldName = worldName;
    this.readOnly = readOnly;
    this.propertyMap = propertyMap;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return AsyncPreLoadWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return AsyncPreLoadWorldEvent.getHandlerList();
  }
}
