package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.file.RealmFormatMessages;
import io.github.portlek.realmformat.paper.file.RealmFormatWorlds;
import io.github.portlek.realmformat.paper.internal.configurate.Configs;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import io.github.portlek.realmformat.paper.module.RealmFormatCommandModule;
import io.github.portlek.realmformat.paper.module.RealmFormatLoaderModule;
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

final class RealmFormatPlugin implements TerminableConsumer, Terminable {

  private static final AtomicReference<RealmFormatPlugin> INSTANCE = new AtomicReference<>();

  @NotNull
  private final RealmFormatBoostrap boostrap;

  @Delegate(types = { TerminableConsumer.class, Terminable.class })
  private final CompositeTerminable terminable = CompositeTerminable.simple();

  private RealmFormatPlugin(@NotNull final RealmFormatBoostrap boostrap) {
    this.boostrap = boostrap;
  }

  @NotNull
  static RealmFormatPlugin get() {
    return Objects.requireNonNull(RealmFormatPlugin.INSTANCE.get());
  }

  static void initialize(@NotNull final RealmFormatBoostrap boostrap) {
    final var plugin = new RealmFormatPlugin(boostrap);
    RealmFormatPlugin.INSTANCE.set(plugin);
    plugin.onLoad();
  }

  void onDisable() {
    this.closeUnchecked();
  }

  void onEnable() {
    final var folder = this.boostrap.getDataFolder().toPath();
    Services
      .provide(
        RealmFormatConfig.class,
        new RealmFormatConfig(Configs.yaml(folder.resolve("config.yaml")))
      )
      .reload();
    Services
      .provide(
        RealmFormatMessages.class,
        new RealmFormatMessages(Configs.yaml(folder.resolve("messages.yaml")))
      )
      .reload();
    Services
      .provide(
        RealmFormatWorlds.class,
        new RealmFormatWorlds(Configs.yaml(folder.resolve("worlds.yaml")))
      )
      .reload();
    Services
      .provide(RealmFormatLoaderModule.class, new RealmFormatLoaderModule())
      .bindModuleWith(this);
    Services
      .provide(RealmFormatCommandModule.class, new RealmFormatCommandModule())
      .bindModuleWith(this);
  }

  private void onLoad() {
    BukkitTasks.init(this.boostrap).bindWith(this);
    Plugins.init(this.boostrap, new PaperEventManager());
  }
}
