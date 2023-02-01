package io.github.portlek.realmformat.paper.api.event;

import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public interface CancellableEvent extends Cancellable {
  @NotNull
  AtomicBoolean cancelled();

  @Override
  default boolean isCancelled() {
    return this.cancelled().get();
  }

  @Override
  default void setCancelled(final boolean cancel) {
    this.cancelled().set(cancel);
  }
}
