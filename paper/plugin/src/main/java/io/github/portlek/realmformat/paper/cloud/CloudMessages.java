package io.github.portlek.realmformat.paper.cloud;

import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry;
import cloud.commandframework.paper.PaperCommandManager;
import lombok.experimental.UtilityClass;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class CloudMessages {

  public static void register(@NotNull final PaperCommandManager<CommandSender> manager) {
    if (manager.captionRegistry() instanceof FactoryDelegatingCaptionRegistry<?> registry) {
    }
  }
}
