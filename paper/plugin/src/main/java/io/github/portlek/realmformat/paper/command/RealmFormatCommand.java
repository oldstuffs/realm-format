package io.github.portlek.realmformat.paper.command;

import io.github.portlek.realmformat.paper.cloud.Cloud;
import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.file.RealmFormatMessages;
import io.github.portlek.realmformat.paper.misc.Services;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.task.Schedulers;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

public final class RealmFormatCommand implements TerminableModule {

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {
    final var config = Services.load(RealmFormatConfig.class);
    final var messages = Services.load(RealmFormatMessages.class);
    final var commandManager = Services.load(Cloud.KEY);
    final var builder = commandManager
      .commandBuilder("realmformat", "rf")
      .permission("realmformat.command.base");
    Cloud.registerHelpCommand(commandManager, builder, "realmformat");
    final var reload = builder
      .literal("reload")
      .permission("realmformat.command.reload")
      .handler(context -> {
        final var now = System.currentTimeMillis();
        Schedulers
          .async()
          .run(() -> {
            config.reload();
            messages.reload();
            messages
              .reloadComplete()
              .sendP(context.getSender(), "took", System.currentTimeMillis() - now);
          })
          .bindWith(consumer);
      });
    commandManager.command(reload);
  }
}
