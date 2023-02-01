package io.github.portlek.realmformat.paper.file;

import static io.github.portlek.realmformat.paper.misc.FormattedMessage.of;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

import io.github.portlek.realmformat.paper.configurate.Config;
import io.github.portlek.realmformat.paper.misc.FormattedMessage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public final class RealmFormatMessages implements Config {

  @NotNull
  private final ConfigurationLoader<?> loader;

  @Setting
  private FormattedMessage reloadComplete = of(text("Reload complete! Took %took%ms").color(GREEN));

  public RealmFormatMessages(@NotNull final ConfigurationLoader<?> loader) {
    this.loader = loader;
  }
}
