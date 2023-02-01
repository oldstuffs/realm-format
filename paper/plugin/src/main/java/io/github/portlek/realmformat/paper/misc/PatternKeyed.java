package io.github.portlek.realmformat.paper.misc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class PatternKeyed {

  private static final Map<PatternKey, PatternKeyed> CACHE = new ConcurrentHashMap<>();

  @NotNull
  private final Pattern compiled;

  @NotNull
  @ToString.Include
  @EqualsAndHashCode.Include
  private final String key;

  private PatternKeyed(@NotNull final String key, @NotNull final Pattern compiled) {
    this.key = key;
    this.compiled = compiled;
  }

  @NotNull
  static PatternKeyed of(@NotNull final String pattern, final int flag) {
    return PatternKeyed.CACHE.computeIfAbsent(new PatternKey(pattern, flag), PatternKey::compile);
  }

  private record PatternKey(@NotNull String pattern, int flag) {
    @NotNull
    private PatternKeyed compile() {
      return new PatternKeyed(this.pattern, Pattern.compile(this.pattern, this.flag));
    }
  }
}
