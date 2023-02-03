package io.github.portlek.realmformat.format.property.type;

import io.github.portlek.realmformat.format.property.RealmFormatProperty;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RealmFormatPropertyInt extends RealmFormatProperty<Integer> {

  public RealmFormatPropertyInt(
    @NotNull final String nbtName,
    @Nullable final Integer defaultValue,
    @Nullable final Predicate<Integer> validator
  ) {
    super(nbtName, defaultValue, validator);
  }

  public RealmFormatPropertyInt(
    @NotNull final String nbtName,
    @Nullable final Integer defaultValue
  ) {
    this(nbtName, defaultValue, null);
  }

  public RealmFormatPropertyInt(@NotNull final String nbtName) {
    this(nbtName, null, null);
  }

  @Nullable
  @Override
  protected Integer readValue(@NotNull final Tag tag) {
    if (tag.isInt()) {
      return this.defaultValue();
    }
    return tag.asInt().intValue();
  }

  @Override
  protected void writeValue(@NotNull final CompoundTag compound, @NotNull final Integer value) {
    compound.setInteger(this.nbtName(), value);
  }
}
