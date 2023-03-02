package io.github.portlek.realmformat.paper;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.realm.RealmFormatSerializers;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrades;
import io.github.portlek.realmformat.modifier.Modifier;
import io.github.portlek.realmformat.modifier.ModifierBackend;
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
import io.github.portlek.realmformat.paper.nms.v1_19_R2.ModifierBackendV1_19_R2;
import io.github.portlek.realmformat.paper.nms.v1_19_R2.NmsBackendV1_19_R2;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.event.paper.PaperEventManager;
import tr.com.infumia.task.BukkitTasks;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.versionmatched.VersionMatched;

public final class RealmFormatPlugin implements Reloadable {

  private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

  private static final VersionMatched<NmsBackend> NMS_BACKEND = new VersionMatched<>(
    NmsBackendV1_19_R2.class
  );

  private static final VersionMatched<ModifierBackend> MODIFIER_BACKEND = new VersionMatched<>(
    ModifierBackendV1_19_R2.class
  );

  private final CompositeTerminable terminable = CompositeTerminable.simple();

  @NotNull
  private final List<Terminable> terminables;

  private RealmFormatPlugin(@NotNull final List<Terminable> terminables) {
    this.terminables = terminables;
  }

  static void initialize(@NotNull final RealmFormatBoostrap boostrap) {
    Plugins.init(boostrap, new PaperEventManager());
    Services.provide(
      RealmFormatPlugin.class,
      new RealmFormatPlugin(List.of(BukkitTasks.init(boostrap)))
    );
    RealmFormatSerializers.initiate();
    RealmFormatWorldUpgrades.initiate();
    Services.provide(RealmFormatBoostrap.class, boostrap);
    Services.provide(Path.class, Services.provide(File.class, boostrap.getDataFolder()).toPath());
    final var nmsBackend = Services.provide(
      NmsBackend.class,
      RealmFormatPlugin.NMS_BACKEND.of().create().orElseThrow()
    );
    Modifier.initiateBackend(
      RealmFormatPlugin.MODIFIER_BACKEND.of(NmsBackend.class).create(nmsBackend).orElseThrow()
    );
    Services.provide(RealmFormatManager.class, new RealmFormatManagerImpl());
    RealmFormatPlugin.INITIALIZED.set(true);
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
    Preconditions.checkState(
      RealmFormatPlugin.INITIALIZED.get(),
      "RealmFormat plugin cannot be initialized properly, please check the logs!"
    );
    Services.provide(Cloud.KEY, Cloud.create(Services.load(RealmFormatBoostrap.class)));
    this.reload();
    Services.load(RealmFormatCommandModule.class).bindModuleWith(this.terminable);
  }
}
