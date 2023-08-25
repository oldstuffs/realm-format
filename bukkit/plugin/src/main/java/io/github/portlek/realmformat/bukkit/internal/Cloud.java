package io.github.portlek.realmformat.bukkit.internal;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public interface Cloud {
    @NotNull
    static AnnotationParser<CommandSender> annotation(
        @NotNull final BukkitCommandManager<CommandSender> commandManager
    ) {
        return new AnnotationParser<>(
            commandManager,
            CommandSender.class,
            parserParameters -> SimpleCommandMeta.empty()
        );
    }

    @NotNull
    @SneakyThrows
    static BukkitCommandManager<CommandSender> create(@NotNull final Plugin plugin) {
        final BukkitCommandManager<@NonNull CommandSender> manager;
        if (Misc.isPaper()) {
            final PaperCommandManager<@NonNull CommandSender> mngr =
                PaperCommandManager.createNative(
                    plugin,
                    CommandExecutionCoordinator.simpleCoordinator()
                );
            try {
                mngr.registerAsynchronousCompletions();
            } catch (final Exception ignored) {}
            manager = mngr;
        } else {
            manager =
            BukkitCommandManager.createNative(
                plugin,
                CommandExecutionCoordinator.simpleCoordinator()
            );
        }
        try {
            manager.registerBrigadier();
        } catch (final Exception ignored) {}
        return manager;
    }
}
