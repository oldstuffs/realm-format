package io.github.portlek.realmformat.paper.file;

import static io.github.portlek.realmformat.paper.internal.misc.FormattedMessage.of;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

import io.github.portlek.realmformat.paper.internal.configurate.Config;
import io.github.portlek.realmformat.paper.internal.configurate.Configs;
import io.github.portlek.realmformat.paper.internal.misc.FormattedMessage;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import java.nio.file.Path;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public final class RealmFormatMessages implements Config {

  @NotNull
  private final ConfigurationLoader<?> loader = Configs.yaml(
    Services.load(Path.class).resolve("messages.yaml")
  );

  @Setting
  private FormattedMessage reloadComplete = of(text("Reload complete! Took %took%ms").color(GREEN));
}
