package io.github.portlek.realmformat.paper.internal.configurate.serializer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NonItalicFallbackComponentSerializer implements LegacyComponentSerializer {

  public static final NonItalicFallbackComponentSerializer INSTANCE =
    new NonItalicFallbackComponentSerializer();

  private static final LegacyComponentSerializer DELEGATE = LegacyComponentSerializer
    .builder()
    .character('&')
    .hexColors()
    .build();

  @NotNull
  @Override
  public TextComponent deserialize(@NotNull final String input) {
    final TextComponent component = NonItalicFallbackComponentSerializer.DELEGATE.deserialize(
      input
    );
    return (TextComponent) component.applyFallbackStyle(TextDecoration.ITALIC.withState(false));
  }

  @NotNull
  @Override
  public String serialize(@NotNull final Component component) {
    return NonItalicFallbackComponentSerializer.DELEGATE.serialize(component);
  }

  @NotNull
  @Override
  public Builder toBuilder() {
    return NonItalicFallbackComponentSerializer.DELEGATE.toBuilder();
  }
}
