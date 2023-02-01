package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.loader.RealmLoader;
import java.io.File;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncPostImportWorldEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  @Getter
  @NotNull
  private final RealmLoader loader;

  @Getter
  @NotNull
  private final File worldDirectory;

  @Getter
  @NotNull
  private final String worldName;

  public AsyncPostImportWorldEvent(
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
    return AsyncPostImportWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return AsyncPostImportWorldEvent.getHandlerList();
  }
}
