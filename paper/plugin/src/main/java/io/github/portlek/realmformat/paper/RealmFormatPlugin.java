package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.file.RealmFormatMessages;
import io.github.portlek.realmformat.paper.file.RealmFormatWorlds;
import io.github.portlek.realmformat.paper.internal.cloud.Cloud;
import io.github.portlek.realmformat.paper.internal.configurate.Configs;
import io.github.portlek.realmformat.paper.internal.misc.Reloadable;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import io.github.portlek.realmformat.paper.module.RealmFormatCommandModule;
import io.github.portlek.realmformat.paper.module.RealmFormatLoaderModule;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.event.paper.PaperEventManager;
import tr.com.infumia.task.BukkitTasks;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;

public final class RealmFormatPlugin implements Reloadable {

  private static final AtomicReference<RealmFormatPlugin> INSTANCE = new AtomicReference<>();

  @NotNull
  private final RealmFormatBoostrap boostrap;

  private final CompositeTerminable terminable = CompositeTerminable.simple();

  @NotNull
  private final List<Terminable> terminables;

  private RealmFormatPlugin(
    @NotNull final RealmFormatBoostrap boostrap,
    @NotNull final List<Terminable> terminables
  ) {
    this.boostrap = boostrap;
    this.terminables = terminables;
  }

  @NotNull
  static RealmFormatPlugin get() {
    return Objects.requireNonNull(RealmFormatPlugin.INSTANCE.get());
  }

  static void initialize(@NotNull final RealmFormatBoostrap boostrap) {
    Plugins.init(boostrap, new PaperEventManager());
    final var plugin = new RealmFormatPlugin(boostrap, List.of(BukkitTasks.init(boostrap)));
    RealmFormatPlugin.INSTANCE.set(plugin);
  }

  @Override
  public void reload() {
    this.onDisable();
    this.terminables.forEach(terminable -> terminable.bindWith(this.terminable));
    final var folder = this.boostrap.getDataFolder().toPath();
    Services.provide(RealmFormatPlugin.class, this);
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
      .bindModuleWith(this.terminable);
  }

  void onDisable() {
    this.terminable.closeUnchecked();
  }

  void onEnable() {
    this.reload();
    Services.provide(Cloud.KEY, Cloud.create(this.boostrap));
    Services
      .provide(RealmFormatCommandModule.class, new RealmFormatCommandModule())
      .bindModuleWith(this.terminable);
  }
}
