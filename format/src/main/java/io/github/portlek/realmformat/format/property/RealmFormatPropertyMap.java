package io.github.portlek.realmformat.format.property;

import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
@EqualsAndHashCode
public final class RealmFormatPropertyMap {

  @NotNull
  private final CompoundTag tag;

  public RealmFormatPropertyMap(@NotNull final CompoundTag tag) {
    this.tag = tag;
  }

  public RealmFormatPropertyMap() {
    this(Tag.createCompound());
  }

  @Nullable
  public <T> T getValue(@NotNull final RealmFormatProperty<T> property) {
    return this.tag.get(property.nbtName())
      .map(property::readValue)
      .orElseGet(property::defaultValue);
  }

  public void merge(@NotNull final CompoundTag tag) {
    tag.all().forEach(this.tag::set);
  }

  public void merge(@NotNull final RealmFormatPropertyMap map) {
    this.merge(map.tag);
  }

  public <T> void setValue(@NotNull final RealmFormatProperty<T> property, @NotNull final T value) {
    if (property.validator() != null && !property.validator().test(value)) {
      throw new IllegalArgumentException("'%s' is not a valid property value.".formatted(value));
    }
    property.writeValue(this.tag, value);
  }
}
