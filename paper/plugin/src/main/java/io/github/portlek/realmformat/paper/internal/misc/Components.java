package io.github.portlek.realmformat.paper.internal.misc;

import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class Components {

  private final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

  @Nullable
  @Contract("null -> null; !null -> !null")
  public Component deserialize(@Nullable final String text) {
    return Optional
      .ofNullable(text)
      .map(Components.LEGACY_SERIALIZER::deserialize)
      .map(component -> component.applyFallbackStyle(TextDecoration.ITALIC.withState(false)))
      .orElse(null);
  }

  @Nullable
  @Contract("null, _ -> null; !null, _ -> !null")
  public List<Component> replace(
    @Nullable final List<Component> components,
    @NotNull final Replace replace
  ) {
    return Optional
      .ofNullable(components)
      .map(c ->
        c
          .stream()
          .map(component -> Components.replace(component, replace))
          .map(component -> component.applyFallbackStyle(TextDecoration.ITALIC.withState(false)))
          .toList()
      )
      .orElse(null);
  }

  @Nullable
  @Contract("null, _ -> null; !null, _-> !null")
  public Component replace(@Nullable final Component component, @NotNull final Replace replace) {
    return Optional
      .ofNullable(component)
      .map(replace::replace)
      .map(c -> c.applyFallbackStyle(TextDecoration.ITALIC.withState(false)))
      .orElse(null);
  }

  @Nullable
  @Contract("null -> null; !null -> !null")
  public String serialize(@Nullable final Component input) {
    return Optional.ofNullable(input).map(Components.LEGACY_SERIALIZER::serialize).orElse(null);
  }
}
