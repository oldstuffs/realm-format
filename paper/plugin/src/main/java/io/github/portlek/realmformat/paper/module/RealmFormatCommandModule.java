package io.github.portlek.realmformat.paper.module;

import cloud.commandframework.Command;
import cloud.commandframework.paper.PaperCommandManager;
import io.github.portlek.realmformat.paper.RealmFormatPlugin;
import io.github.portlek.realmformat.paper.internal.Cloud;
import jdk.vm.ci.services.Services;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

public final class RealmFormatCommandModule implements TerminableModule {

    @Override
    public void setup(@NotNull final TerminableConsumer consumer) {
        final RealmFormatPlugin plugin = Services.load(RealmFormatPlugin.class);
        final RealmFormatMessages messages = Services.load(RealmFormatMessages.class);
        final PaperCommandManager<CommandSender> commandManager = Services.load(Cloud.KEY);
        final Command.Builder<CommandSender> builder = commandManager.commandBuilder(
            "realmformat",
            "rf"
        );
        commandManager.command(
            builder
                .literal("reload")
                .handler(context -> {
                    final long now = System.currentTimeMillis();
                    Schedulers
                        .async()
                        .run(() -> {
                            plugin.reload();
                            messages
                                .reloadComplete()
                                .sendP(
                                    context.getSender(),
                                    "took",
                                    System.currentTimeMillis() - now
                                );
                        })
                        .bindWith(consumer);
                })
        );
    }
}
