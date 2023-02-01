package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.loader.RealmLoader;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPreImportWorldEvent extends Event implements CancellableEvent {

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
  private File worldDirectory;

  @Getter
  @Setter
  @NotNull
  private String worldName;

  public AsyncPreImportWorldEvent(
    @NotNull final File worldDirectory,
    @NotNull final String worldName,
    @NotNull final RealmLoader loader
  ) {
    super(true);
    this.worldDirectory = worldDirectory;
    this.worldName = worldName;
    this.loader = loader;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return AsyncPreImportWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return AsyncPreImportWorldEvent.getHandlerList();
  }
}
