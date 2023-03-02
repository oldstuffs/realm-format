package io.github.portlek.realmformat.format.property;

import com.google.common.base.Preconditions;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString(onlyExplicitlyIncluded = true)
public abstract class RealmFormatProperty<T> {

  @Nullable
  @ToString.Include
  private final T defaultValue;

  @NotNull
  @ToString.Include
  private final String nbtName;

  @Nullable
  private final Predicate<@NotNull T> validator;

  protected RealmFormatProperty(
    @NotNull final String nbtName,
    @Nullable final T defaultValue,
    @Nullable final Predicate<T> validator
  ) {
    this.nbtName = nbtName;
    Preconditions.checkArgument(
      validator == null || defaultValue == null || validator.test(defaultValue),
      "Invalid default value for property %s! %s",
      nbtName,
      defaultValue
    );
    this.defaultValue = defaultValue;
    this.validator = validator;
  }

  @Nullable
  protected abstract T readValue(@NotNull Tag tag);

  protected abstract void writeValue(@NotNull CompoundTag compound, @NotNull T value);
}
