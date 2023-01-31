package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.modifier.Modifier;
import io.github.portlek.realmformat.modifier.ModifierBackend;
import io.github.portlek.realmformat.paper.cloud.Cloud;
import io.github.portlek.realmformat.paper.command.RealmFormatCommand;
import io.github.portlek.realmformat.paper.configurate.Configs;
import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.file.RealmFormatMessages;
import io.github.portlek.realmformat.paper.misc.Services;
import io.github.portlek.realmformat.paper.nms.v1_18_R2.ModifierBackend1_18_R2;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.event.paper.PaperEventManager;
import tr.com.infumia.task.BukkitTasks;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.versionmatched.VersionMatched;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class RealmFormatPlugin implements TerminableConsumer, Terminable {

  private static final VersionMatched<ModifierBackend> BACKEND_VERSION_MATCHED = new VersionMatched<>(
    ModifierBackend1_18_R2.class
  );

  private static final AtomicReference<RealmFormatPlugin> INSTANCE = new AtomicReference<>();

  @NotNull
  RealmFormatBoostrap boostrap;

  @Delegate(types = { TerminableConsumer.class, Terminable.class })
  CompositeTerminable terminable = CompositeTerminable.simple();

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
    Services.provide(Cloud.KEY, Cloud.create(this.boostrap));
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
    new RealmFormatCommand().bindModuleWith(this);
  }

  private void onLoad() {
    BukkitTasks.init(this.boostrap).bindWith(this);
    Plugins.init(this.boostrap, new PaperEventManager());
    Modifier.initiateBackend(
      RealmFormatPlugin.BACKEND_VERSION_MATCHED
        .of()
        .create()
        .orElseThrow(() -> new UnsupportedOperationException("This version is not supported!"))
    );
  }
}
