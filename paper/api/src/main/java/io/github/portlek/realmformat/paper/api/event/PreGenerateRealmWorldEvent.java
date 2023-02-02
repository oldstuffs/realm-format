package io.github.portlek.realmformat.paper.api.event;

import io.github.portlek.realmformat.format.old.realm.RealmWorld;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PreGenerateRealmWorldEvent extends RealmWorldEvent implements CancellableEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  @Getter
  private final AtomicBoolean cancelled = new AtomicBoolean();

  public PreGenerateRealmWorldEvent(@NotNull final RealmWorld world) {
    super(false, world);
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return PreGenerateRealmWorldEvent.HANDLER_LIST;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return PreGenerateRealmWorldEvent.getHandlerList();
  }

  @NotNull
  public PreGenerateRealmWorldEvent world(@NotNull final RealmWorld world) {
    this.world = world;
    return this;
  }
}
