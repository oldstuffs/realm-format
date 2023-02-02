package io.github.portlek.realmformat.format.property;

import com.google.common.base.Preconditions;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
public final class RealmFormatPropertyMap {

  @NotNull
  private final CompoundTag tag;

  public RealmFormatPropertyMap(@NotNull final CompoundTag tag) {
    this.tag = tag;
  }

  public RealmFormatPropertyMap() {
    this(Tag.createCompound());
  }

  @NotNull
  public <T> T getValue(@NotNull final RealmFormatProperty<T> property) {
    return this.tag.get(property.nbtName())
      .map(property::readValue)
      .orElse(property.defaultValue());
  }

  public void merge(@NotNull final CompoundTag tag) {
    tag.all().forEach(this.tag::set);
  }

  public void merge(@NotNull final RealmFormatPropertyMap map) {
    this.merge(map.tag);
  }

  public <T> void setValue(@NotNull final RealmFormatProperty<T> property, @NotNull final T value) {
    Preconditions.checkArgument(
      property.validator() == null || property.validator().test(value),
      "'%s' is not a valid property value.",
      value
    );
    property.writeValue(this.tag, value);
  }
}
