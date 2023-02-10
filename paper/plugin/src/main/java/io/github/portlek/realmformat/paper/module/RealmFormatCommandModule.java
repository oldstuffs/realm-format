package io.github.portlek.realmformat.paper.module;

import io.github.portlek.realmformat.paper.RealmFormatPlugin;
import io.github.portlek.realmformat.paper.file.RealmFormatMessages;
import io.github.portlek.realmformat.paper.internal.cloud.Cloud;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.task.Schedulers;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

@Log4j2
public final class RealmFormatCommandModule implements TerminableModule {

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {
    final var plugin = Services.load(RealmFormatPlugin.class);
    final var messages = Services.load(RealmFormatMessages.class);
    final var commandManager = Services.load(Cloud.KEY);
    final var builder = commandManager.commandBuilder("realmformat", "rf");
    Cloud.registerHelpCommand(commandManager, builder, "realmformat");
    builder
      .literal("reload")
      .handler(context -> {
        final var now = System.currentTimeMillis();
        Schedulers
          .async()
          .run(() -> {
            plugin.reload();
            messages
              .reloadComplete()
              .sendP(context.getSender(), "took", System.currentTimeMillis() - now);
          })
          .bindWith(consumer);
      });
  }
}
