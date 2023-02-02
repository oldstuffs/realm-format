package io.github.portlek.realmformat.paper;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.event.paper.PaperEventManager;
import tr.com.infumia.task.BukkitTasks;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.terminable.TerminableConsumer;

final class RealmPlugin implements TerminableConsumer, Terminable {

  private static final AtomicReference<RealmPlugin> INSTANCE = new AtomicReference<>();

  @NotNull
  private final RealmBoostrap boostrap;

  @Delegate(types = { TerminableConsumer.class, Terminable.class })
  private final CompositeTerminable terminable = CompositeTerminable.simple();

  private RealmPlugin(@NotNull final RealmBoostrap boostrap) {
    this.boostrap = boostrap;
  }

  @NotNull
  static RealmPlugin get() {
    return Objects.requireNonNull(RealmPlugin.INSTANCE.get());
  }

  static void initialize(@NotNull final RealmBoostrap boostrap) {
    final var plugin = new RealmPlugin(boostrap);
    RealmPlugin.INSTANCE.set(plugin);
    plugin.onLoad();
  }

  void onDisable() {
    this.closeUnchecked();
  }

  void onEnable() {
    final var folder = this.boostrap.getDataFolder().toPath();
  }

  private void onLoad() {
    BukkitTasks.init(this.boostrap).bindWith(this);
    Plugins.init(this.boostrap, new PaperEventManager());
  }
}
