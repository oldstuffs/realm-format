package io.github.portlek.realmformat.bukkit;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.CommandMethod;
import io.github.portlek.realmformat.bukkit.config.RealmFormatMessages;
import io.github.portlek.realmformat.bukkit.internal.Misc;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.task.Schedulers;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

final class RealmFormatCommandModule implements TerminableModule {

    @NotNull
    private final AnnotationParser<CommandSender> annotationParser;

    @NotNull
    private final RealmFormatMessages messages;

    @NotNull
    private final RealmFormatPlugin plugin;

    RealmFormatCommandModule(
        @NotNull final RealmFormatPlugin plugin,
        @NotNull final AnnotationParser<CommandSender> annotationParser,
        @NotNull final RealmFormatMessages messages
    ) {
        this.plugin = plugin;
        this.annotationParser = annotationParser;
        this.messages = messages;
    }

    @Override
    public void setup(@NotNull final TerminableConsumer consumer) {
        this.annotationParser.parse(new Cmd(this.plugin, this.messages));
    }

    @CommandMethod("realmformat|rf")
    private static final class Cmd {

        @NotNull
        private final RealmFormatMessages messages;

        @NotNull
        private final RealmFormatPlugin plugin;

        private Cmd(
            @NotNull final RealmFormatPlugin plugin,
            @NotNull final RealmFormatMessages messages
        ) {
            this.plugin = plugin;
            this.messages = messages;
        }

        @CommandMethod("reload")
        public void reload(final CommandSender sender) {
            final long now = System.currentTimeMillis();
            Schedulers
                .async()
                .run(() -> {
                    this.plugin.reload();
                    sender.sendMessage(
                        Misc.colorize(
                            this.messages.reloadComplete()
                                .replace("%took%", String.valueOf(System.currentTimeMillis() - now))
                        )
                    );
                })
                .bindWith(this.plugin);
        }
    }
}
