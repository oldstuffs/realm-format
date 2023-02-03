package io.github.portlek.realmformat.format.property.type;

import io.github.portlek.realmformat.format.property.RealmFormatProperty;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RealmFormatPropertyBoolean extends RealmFormatProperty<Boolean> {

  public RealmFormatPropertyBoolean(
    @NotNull final String nbtName,
    @NotNull final Boolean defaultValue,
    @Nullable final Predicate<Boolean> validator
  ) {
    super(nbtName, defaultValue, validator);
  }

  public RealmFormatPropertyBoolean(
    @NotNull final String nbtName,
    @NotNull final Boolean defaultValue
  ) {
    this(nbtName, defaultValue, null);
  }

  @Override
  protected Boolean readValue(@NotNull final Tag tag) {
    if (tag.isByte()) {
      return this.defaultValue();
    }
    return tag.asByte().byteValue() == 1;
  }

  @Override
  protected void writeValue(@NotNull final CompoundTag compound, @NotNull final Boolean value) {
    compound.setByte(this.nbtName(), value ? (byte) 1 : (byte) 0);
  }
}
