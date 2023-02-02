package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.format.exception.UnknownWorldException;
import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.portlek.realmformat.paper.api.RealmManager;
import io.github.portlek.realmformat.paper.cloud.Cloud;
import io.github.portlek.realmformat.paper.command.RealmCommand;
import io.github.portlek.realmformat.paper.configurate.Configs;
import io.github.portlek.realmformat.paper.file.RealmConfig;
import io.github.portlek.realmformat.paper.file.RealmMessages;
import io.github.portlek.realmformat.paper.file.RealmWorlds;
import io.github.portlek.realmformat.paper.misc.Misc;
import io.github.portlek.realmformat.paper.misc.Services;
import io.github.portlek.realmformat.paper.nms.RealmNmsBackend;
import io.github.portlek.realmformat.paper.nms.v1_18_R2.RealmNmsBackend1_18_R2;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import lombok.experimental.Delegate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.event.paper.PaperEventManager;
import tr.com.infumia.task.BukkitTasks;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.versionmatched.VersionMatched;

final class RealmPlugin implements TerminableConsumer, Terminable {

  private static final RealmNmsBackend BACKEND_VERSION_MATCHED = new VersionMatched<>(
    RealmNmsBackend1_18_R2.class
  )
    .ofPrimitive(boolean.class)
    .create(Misc.isPaper())
    .orElseThrow(() -> new UnsupportedOperationException("This version is not supported!"));

  private static final AtomicReference<RealmPlugin> INSTANCE = new AtomicReference<>();

  @NotNull
  private final RealmBoostrap boostrap;

  @Delegate(types = {TerminableConsumer.class, Terminable.class})
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
    Services.provide(Cloud.KEY, Cloud.create(this.boostrap));
    Services
      .provide(RealmConfig.class, new RealmConfig(Configs.yaml(folder.resolve("config.yaml"))))
      .reload();
    Services
      .provide(
        RealmMessages.class,
        new RealmMessages(Configs.yaml(folder.resolve("messages.yaml")))
      )
      .reload();
    Services
      .provide(RealmWorlds.class, new RealmWorlds(Configs.yaml(folder.resolve("worlds.yaml"))))
      .reload();
    final var nms = Services.provide(RealmNmsBackend.class, RealmPlugin.BACKEND_VERSION_MATCHED);
    Services.provide(RealmManager.class, this.bindModule(new RealmManagerImpl()));
    new RealmCommand().bindModuleWith(this);
    this.bind(
      (Terminable) () ->
        Bukkit
          .getWorlds()
          .stream()
          .map(nms::realmWorld)
          .filter(Objects::nonNull)
          .filter(Predicate.not(RealmWorld::readOnly))
          .forEach(world -> {
            try {
              final var loader = world.loader();
              final var name = world.name();
              loader.saveWorld(name, world.serialize().join(), world.locked());
              if (loader.isWorldLocked(name)) {
                loader.unlockWorld(name);
              }
            } catch (final IOException | UnknownWorldException e) {
              e.printStackTrace();
            }
          })
    );
  }

  private void onLoad() {
    BukkitTasks.init(this.boostrap).bindWith(this);
    Plugins.init(this.boostrap, new PaperEventManager());
  }
}
