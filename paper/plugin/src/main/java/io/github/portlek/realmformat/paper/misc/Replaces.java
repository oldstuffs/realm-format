package io.github.portlek.realmformat.paper.misc;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Replaces {

  private final Replace EMPTY = Replaces.of("", "", Collections.emptyMap());

  private final Replace EMPTY_CURLY_BRACKET = Replaces.cb(Collections.emptyMap());

  private final Replace EMPTY_PERCENTAGES = Replaces.p(Collections.emptyMap());

  @NotNull
  public Replace cb(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return Replaces.cb(Replaces.combine(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace cb(@NotNull final Map<?, ?> replaces) {
    return Replaces.of("{", "}", replaces);
  }

  @NotNull
  public Replace empty() {
    return Replaces.EMPTY;
  }

  @NotNull
  public Replace emptyCb() {
    return Replaces.EMPTY_CURLY_BRACKET;
  }

  @NotNull
  public Replace emptyP() {
    return Replaces.EMPTY_PERCENTAGES;
  }

  @NotNull
  public Replace of(
    @NotNull final Object prefix,
    @NotNull final Object suffix,
    @NotNull final Map<?, ?> replaces
  ) {
    return Replace.of(Replaces.compile(prefix, suffix, replaces, Pattern.LITERAL));
  }

  @NotNull
  public Replace p(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return Replaces.p(Replaces.combine(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace p(@NotNull final Map<?, ?> replaces) {
    return Replaces.of("%", "%", replaces);
  }

  @NotNull
  public Replace pattern(@NotNull final Map<?, ?> replaces) {
    return Replace.of(Replaces.compile(replaces, 0));
  }

  @NotNull
  public Replace pattern(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return Replaces.pattern(Replaces.combine(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace patternCaseInsensitive(@NotNull final Map<?, ?> replaces) {
    return Replace.of(Replaces.compile(replaces, Pattern.CASE_INSENSITIVE));
  }

  @NotNull
  public Replace patternCaseInsensitive(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return Replaces.patternCaseInsensitive(Replaces.combine(match1, replace1, matchAndReplaces));
  }

  @NotNull
  public Replace patternLiteral(@NotNull final Map<?, ?> replaces) {
    return Replace.of(Replaces.compile(replaces, Pattern.LITERAL));
  }

  @NotNull
  public Replace patternLiteral(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    return Replaces.patternLiteral(Replaces.combine(match1, replace1, matchAndReplaces));
  }

  @NotNull
  private Map<?, ?> combine(
    @NotNull final Object match1,
    @NotNull final Object replace1,
    @NotNull final Object @NotNull... matchAndReplaces
  ) {
    Preconditions.checkArgument(
      matchAndReplaces.length % 2 == 0,
      "'matchAndReplaces' must be even!"
    );
    final var map = new HashMap<>();
    for (var index = 0; index < matchAndReplaces.length; index += 2) {
      map.put(matchAndReplaces[index], matchAndReplaces[index + 1]);
    }
    map.put(match1, replace1);
    return map;
  }

  @NotNull
  private Map<PatternKeyed, Object> compile(
    @NotNull final Object prefix,
    @NotNull final Object suffix,
    @NotNull final Map<?, ?> replaces,
    final int flags
  ) {
    final var compiled = new HashMap<PatternKeyed, Object>();
    replaces.forEach((s, o) -> {
      final var pattern = prefix + s.toString() + suffix;
      compiled.put(PatternKeyed.of(pattern, flags), o);
    });
    return compiled;
  }

  @NotNull
  private Map<PatternKeyed, Object> compile(@NotNull final Map<?, ?> replaces, final int flags) {
    return Replaces.compile("", "", replaces, flags);
  }
}
