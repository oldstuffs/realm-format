package io.github.portlek.realmformat.paper.misc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

/**
 * {@link #replacements}'s values support {@link String}, {@link Component}, {@link Supplier}.
 */
public final class Replace {

  @NotNull
  private final Map<PatternKeyed, Object> replacements;

  private Replace(@NotNull final Map<PatternKeyed, Object> replacements) {
    this.replacements = Collections.unmodifiableMap(replacements);
  }

  @NotNull
  public static Replace of(@NotNull final Map<PatternKeyed, Object> pattern) {
    return new Replace(pattern);
  }

  @NotNull
  public Replace merge(@NotNull final Replace replace) {
    final var merge = new HashMap<>(this.replacements);
    merge.putAll(replace.replacements);
    return Replace.of(merge);
  }

  @NotNull
  public Replace mergeCb(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return this.merge(Replaces.cb(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace mergeCb(@NotNull final Map<?, ?> replaces) {
    return this.merge(Replaces.cb(replaces));
  }

  @NotNull
  public Replace mergeP(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return this.merge(Replaces.p(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace mergeP(@NotNull final Map<?, ?> replaces) {
    return this.merge(Replaces.p(replaces));
  }

  @NotNull
  public Replace mergePattern(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return this.merge(Replaces.pattern(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace mergePattern(@NotNull final Map<?, ?> replaces) {
    return this.merge(Replaces.pattern(replaces));
  }

  @NotNull
  public Replace patternCaseInsensitive(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return this.merge(Replaces.patternCaseInsensitive(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace patternCaseInsensitive(@NotNull final Map<?, ?> replaces) {
    return this.merge(Replaces.patternCaseInsensitive(replaces));
  }

  @NotNull
  public Replace patternLiteral(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return this.merge(Replaces.patternLiteral(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace patternLiteral(@NotNull final Map<?, ?> replaces) {
    return this.merge(Replaces.patternLiteral(replaces));
  }

  @NotNull
  public Component replace(@NotNull final Component component) {
    final var result = new AtomicReference<>(component);
    this.replacements.forEach((pattern, replace) ->
        result.updateAndGet(old ->
          old.replaceText(builder -> this.replace(builder.match(pattern.compiled()), replace))
        )
      );
    return result.get();
  }

  @NotNull
  public String replace(@NotNull final String text) {
    final var result = new AtomicReference<>(text);
    this.replacements.forEach((pattern, value) ->
        result.updateAndGet(old -> pattern.compiled().matcher(old).replaceAll(value.toString()))
      );
    return result.get();
  }

  private void replace(
    @NotNull final TextReplacementConfig.Builder config,
    @NotNull final Object replace
  ) {
    if (replace instanceof Supplier<?> supplier) {
      this.replace(config, supplier.get());
    } else if (replace instanceof ComponentLike componentLike) {
      config.replacement(componentLike);
    } else {
      config.replacement(replace.toString());
    }
  }
}
