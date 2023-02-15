package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.format.realm.RealmFormatSerializers;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrades;
import io.github.portlek.realmformat.paper.api.RealmFormatManager;
import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.file.RealmFormatMessages;
import io.github.portlek.realmformat.paper.file.RealmFormatWorlds;
import io.github.portlek.realmformat.paper.internal.cloud.Cloud;
import io.github.portlek.realmformat.paper.internal.misc.Reloadable;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import io.github.portlek.realmformat.paper.module.RealmFormatCommandModule;
import io.github.portlek.realmformat.paper.module.RealmFormatLoaderModule;
import io.github.portlek.realmformat.paper.nms.NmsBackend;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.event.paper.PaperEventManager;
import tr.com.infumia.task.BukkitTasks;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.versionmatched.VersionMatched;

public final class RealmFormatPlugin implements Reloadable {

  private static final AtomicReference<RealmFormatPlugin> INSTANCE = new AtomicReference<>();

  private static final VersionMatched<NmsBackend> NMS_BACKEND = new VersionMatched<>();

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
    RealmFormatSerializers.initiate();
    RealmFormatWorldUpgrades.initiate();
    RealmFormatPlugin.INSTANCE.set(plugin);
  }

  @Override
  public void reload() {
    this.onDisable();
    this.terminables.forEach(terminable -> terminable.bindWith(this.terminable));
    Services.load(RealmFormatConfig.class).reload();
    Services.load(RealmFormatMessages.class).reload();
    Services.load(RealmFormatWorlds.class).reload();
    Services.load(RealmFormatLoaderModule.class).bindModuleWith(this.terminable);
  }

  void onDisable() {
    this.terminable.closeUnchecked();
  }

  void onEnable() {
    Services.provide(RealmFormatBoostrap.class, this.boostrap);
    Services.provide(File.class, this.boostrap.getDataFolder());
    Services.provide(Path.class, this.boostrap.getDataFolder().toPath());
    Services.provide(RealmFormatPlugin.class, this);
    Services.provide(Cloud.KEY, Cloud.create(this.boostrap));
    Services.provide(NmsBackend.class, RealmFormatPlugin.NMS_BACKEND.of().create().orElseThrow());
    Services.provide(RealmFormatManager.class, new RealmFormatManagerImpl());
    this.reload();
    Services.load(RealmFormatCommandModule.class).bindModuleWith(this.terminable);
  }
}
