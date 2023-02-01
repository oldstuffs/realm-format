package io.github.portlek.realmformat.paper.cloud;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.common.reflect.TypeToken;
import java.util.List;
import java.util.function.BiFunction;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Cloud {
  BiFunction<CommandContext<CommandSender>, String, List<String>> DATASOURCE_SUGGESTER = (
    context,
    input
  ) -> {
    return List.of("");
  };

  TypeToken<PaperCommandManager<CommandSender>> KEY = new TypeToken<>() {};

  @NotNull
  @SneakyThrows
  static PaperCommandManager<CommandSender> create(@NotNull final Plugin plugin) {
    final var manager = PaperCommandManager.createNative(
      plugin,
      CommandExecutionCoordinator.simpleCoordinator()
    );
    manager.registerBrigadier();
    manager.registerAsynchronousCompletions();
    CloudMessages.register(manager);
    return manager;
  }

  static void registerHelpCommand(
    @NotNull final PaperCommandManager<CommandSender> commandManager,
    @NotNull final Command.Builder<CommandSender> builder,
    @NotNull final String command
  ) {
    Cloud.registerHelpCommand(commandManager, builder, command, null);
  }

  static void registerHelpCommand(
    @NotNull final PaperCommandManager<CommandSender> commandManager,
    @NotNull final Command.Builder<CommandSender> builder,
    @NotNull final String command,
    @Nullable final String permission
  ) {
    final var helpHandler = MinecraftHelp.createNative("/" + command + " help", commandManager);
    helpHandler.commandFilter(c ->
      c.getArguments().stream().findFirst().orElseThrow().getName().equals(command)
    );
    final var help = builder
      .literal("help", "?")
      .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
      .handler(context ->
        helpHandler.queryCommands(context.getOrDefault("query", ""), context.getSender())
      );
    commandManager.command(permission == null ? help : help.permission(permission));
  }
}
