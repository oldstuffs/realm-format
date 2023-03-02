package io.github.portlek.realmformat.paper.internal.misc;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
@NoArgsConstructor
public final class FormattedMessage {

  @Setting(nodeFromParent = true)
  private Component component;

  private FormattedMessage(final Component component) {
    this.component = component;
  }

  @NotNull
  public static FormattedMessage of(@NotNull final Component component) {
    return new FormattedMessage(component);
  }

  public void broadcast(@NotNull final Replace replace) {
    Bukkit.getOnlinePlayers().forEach(player -> this.send(player, replace));
  }

  @NotNull
  public Component replace(@NotNull final Replace replace) {
    return Components.replace(this.component, replace);
  }

  @NotNull
  public Component replaceCb(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return this.replace(Replaces.cb(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Component replaceP(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return this.replace(Replaces.p(match1, replace1, matchAndReplaces));
  }

  public void send(@NotNull final Audience audience, @NotNull final Replace replace) {
    audience.sendMessage(this.replace(replace));
  }

  public void send(@NotNull final Audience audience) {
    this.send(audience, Replaces.empty());
  }

  public void sendCb(
    @NotNull final Audience audience,
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    this.send(audience, Replaces.cb(match1, replace1, matchAndReplaces));
  }

  public void sendP(
    @NotNull final Audience audience,
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    this.send(audience, Replaces.p(match1, replace1, matchAndReplaces));
  }
}
