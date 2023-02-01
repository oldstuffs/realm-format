package io.github.portlek.realmformat.format.property;

import com.google.common.base.Preconditions;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
public final class RealmPropertyMap {

  @NotNull
  private final CompoundTag tag;

  public RealmPropertyMap(@NotNull final CompoundTag tag) {
    this.tag = tag;
  }

  public RealmPropertyMap() {
    this(Tag.createCompound());
  }

  @NotNull
  public <T> T getValue(@NotNull final RealmProperty<T> property) {
    return this.tag.get(property.nbtName())
      .map(property::readValue)
      .orElse(property.defaultValue());
  }

  public void merge(@NotNull final RealmPropertyMap map) {
    map.tag.all().forEach(this.tag::set);
  }

  public <T> void setValue(@NotNull final RealmProperty<T> property, @NotNull final T value) {
    Preconditions.checkArgument(
      property.validator() == null || property.validator().test(value),
      "'%s' is not a valid property value.",
      value
    );
    property.writeValue(this.tag, value);
  }
}
