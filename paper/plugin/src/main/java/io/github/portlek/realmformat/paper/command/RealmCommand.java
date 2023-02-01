package io.github.portlek.realmformat.paper.command;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import io.github.portlek.realmformat.format.exception.WorldAlreadyExistsException;
import io.github.portlek.realmformat.paper.api.RealmManager;
import io.github.portlek.realmformat.paper.cloud.Cloud;
import io.github.portlek.realmformat.paper.file.RealmConfig;
import io.github.portlek.realmformat.paper.file.RealmMessages;
import io.github.portlek.realmformat.paper.file.RealmWorlds;
import io.github.portlek.realmformat.paper.misc.Components;
import io.github.portlek.realmformat.paper.misc.Services;
import io.github.portlek.realmformat.paper.misc.WorldData;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import lombok.extern.log4j.Log4j2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.task.Schedulers;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

@Log4j2
public final class RealmCommand implements TerminableModule {

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {
    final var worldsInUse = new HashSet<String>();
    final var config = Services.load(RealmConfig.class);
    final var messages = Services.load(RealmMessages.class);
    final var worlds = Services.load(RealmWorlds.class);
    final var manager = Services.load(RealmManager.class);
    final BiFunction<CommandContext<CommandSender>, String, List<String>> datasourceSuggestions = (
      context,
      input
    ) -> {
      return manager
        .availableLoaders()
        .keySet()
        .stream()
        .filter(name -> StringUtil.startsWithIgnoreCase(name, input))
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();
    };
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
            worlds.reload();
            messages
              .reloadComplete()
              .sendP(context.getSender(), "took", System.currentTimeMillis() - now);
          })
          .bindWith(consumer);
      });
    final var create = builder
      .literal("create")
      .argument(
        StringArgument
          .<CommandSender>builder("world")
          .single()
          .withSuggestionsProvider((context, input) ->
            input.isEmpty() ? List.of("<world>") : List.of(input)
          )
          .build()
      )
      .argument(
        StringArgument
          .<CommandSender>builder("datasource")
          .single()
          .withSuggestionsProvider(datasourceSuggestions)
          .build()
      )
      .permission("realmformat.command.create")
      .handler(context -> {
        final var worldName = context.<String>get("world");
        final var datasource = context.<String>get("datasource");
        final var sender = context.getSender();
        if (worldsInUse.contains(worldName)) {
          sender.sendMessage(
            Components.deserialize(
              "&cWorld " +
              worldName +
              " is already being used on another command! Wait some time and try again."
            )
          );
          return;
        }
        final var world = Bukkit.getWorld(worldName);
        if (world != null) {
          sender.sendMessage(Components.deserialize("&cWorld " + worldName + " already exists!"));
          return;
        }
        if (worlds.worlds().containsKey(worldName)) {
          sender.sendMessage(
            Components.deserialize(
              "&cThere is already a world called  " + worldName + " inside the worlds config file."
            )
          );
          return;
        }
        final var loader = manager.loader(datasource);
        if (loader == null) {
          sender.sendMessage(Components.deserialize("&cUnknown data source  " + datasource + "."));
          return;
        }
        worldsInUse.add(worldName);
        sender.sendMessage(
          Components.deserialize("&7Creating empty world &e" + worldName + "&7...")
        );
        Schedulers
          .async()
          .run(() -> {
            try {
              final var start = System.currentTimeMillis();
              final var worldData = new WorldData();
              worldData.spawn("0, 64, 0");
              worldData.dataSource(datasource);
              final var propertyMap = worldData.toPropertyMap();
              final var realmWorld = manager.createEmptyWorld(
                loader,
                worldName,
                false,
                propertyMap
              );
              Schedulers
                .sync()
                .run(() -> {
                  try {
                    manager.generateWorld(realmWorld);
                    final var location = new Location(Bukkit.getWorld(worldName), 0, 61, 0);
                    location.getBlock().setType(Material.BEDROCK);
                    worlds.worlds().put(worldName, worldData);
                    config.save();
                    sender.sendMessage(
                      Components.deserialize(
                        "&aWorld &e" +
                        worldName +
                        "&a created in " +
                        (System.currentTimeMillis() - start) +
                        "ms!"
                      )
                    );
                  } catch (final IllegalArgumentException ex) {
                    sender.sendMessage(
                      Components.deserialize(
                        "&cFailed to create world " + worldName + ": " + ex.getMessage() + "."
                      )
                    );
                  }
                })
                .bindWith(consumer);
            } catch (final WorldAlreadyExistsException ex) {
              sender.sendMessage(
                Components.deserialize(
                  "&cFailed to create world " +
                  worldName +
                  ": world already exists (using data source '" +
                  datasource +
                  "')."
                )
              );
            } catch (final IOException ex) {
              if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(
                  Components.deserialize(
                    "&cFailed to create world " +
                    worldName +
                    ". Take a look at the server console for more information."
                  )
                );
              }
              RealmCommand.log.error("Failed to load world " + worldName + ":");
              ex.printStackTrace();
            } finally {
              worldsInUse.remove(worldName);
            }
          })
          .bindWith(consumer);
      });
    commandManager.command(reload).command(create);
  }
}
