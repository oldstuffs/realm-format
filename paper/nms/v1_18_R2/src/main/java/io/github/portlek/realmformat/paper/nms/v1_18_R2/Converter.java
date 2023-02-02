package io.github.portlek.realmformat.paper.nms.v1_18_R2;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.DataLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log4j2
@UtilityClass
class Converter {

  @NotNull
  static DataLayer convertArray(@NotNull final NibbleArray array) {
    return new DataLayer(array.backing());
  }

  @Nullable
  static NibbleArray convertArray(@Nullable final DataLayer array) {
    if (array == null) {
      return null;
    }
    return new NibbleArray(array.getData());
  }

  @NotNull
  static Tag convertTag(@NotNull final io.github.shiruka.nbt.Tag tag) {
    if (tag.isByte()) {
      return ByteTag.valueOf(tag.asByte().byteValue());
    }
    if (tag.isShort()) {
      return ShortTag.valueOf(tag.asShort().shortValue());
    }
    if (tag.isInt()) {
      return IntTag.valueOf(tag.asInt().intValue());
    }
    if (tag.isLong()) {
      return LongTag.valueOf(tag.asLong().longValue());
    }
    if (tag.isFloat()) {
      return FloatTag.valueOf(tag.asFloat().floatValue());
    }
    if (tag.isDouble()) {
      return DoubleTag.valueOf(tag.asDouble().doubleValue());
    }
    if (tag.isByteArray()) {
      return new ByteArrayTag(tag.asByteArray().primitiveValue());
    }
    if (tag.isIntArray()) {
      return new IntArrayTag(tag.asIntArray().primitiveValue());
    }
    if (tag.isLongArray()) {
      return new LongArrayTag(tag.asLongArray().primitiveValue());
    }
    if (tag.isString()) {
      return StringTag.valueOf(tag.asString().value());
    }
    if (tag.isList()) {
      final var list = new ListTag();
      for (final var innerTag : tag.asList()) {
        list.add(Converter.convertTag(innerTag));
      }
      return list;
    }
    if (tag.isCompound()) {
      final var compound = new CompoundTag();
      tag
        .asCompound()
        .all()
        .forEach((key, value) -> compound.put(key, Converter.convertTag(value)));
      return compound;
    }
    Converter.log.error("Failed to convert NBT object:");
    Converter.log.error(tag.toString());
    throw new IllegalArgumentException("Invalid tag type " + tag.getType().name());
  }

  @NotNull
  static io.github.shiruka.nbt.Tag convertTag(@NotNull final Tag base) {
    switch (base.getId()) {
      case Tag.TAG_BYTE -> {
        return io.github.shiruka.nbt.Tag.createByte(((ByteTag) base).getAsByte());
      }
      case Tag.TAG_SHORT -> {
        return io.github.shiruka.nbt.Tag.createShort(((ShortTag) base).getAsShort());
      }
      case Tag.TAG_INT -> {
        return io.github.shiruka.nbt.Tag.createInt(((IntTag) base).getAsInt());
      }
      case Tag.TAG_LONG -> {
        return io.github.shiruka.nbt.Tag.createLong(((LongTag) base).getAsLong());
      }
      case Tag.TAG_FLOAT -> {
        return io.github.shiruka.nbt.Tag.createFloat(((FloatTag) base).getAsFloat());
      }
      case Tag.TAG_DOUBLE -> {
        return io.github.shiruka.nbt.Tag.createDouble(((DoubleTag) base).getAsDouble());
      }
      case Tag.TAG_BYTE_ARRAY -> {
        return io.github.shiruka.nbt.Tag.createByteArray(((ByteArrayTag) base).getAsByteArray());
      }
      case Tag.TAG_INT_ARRAY -> {
        return io.github.shiruka.nbt.Tag.createIntArray(((IntArrayTag) base).getAsIntArray());
      }
      case Tag.TAG_LONG_ARRAY -> {
        return io.github.shiruka.nbt.Tag.createLongArray(((LongArrayTag) base).getAsLongArray());
      }
      case Tag.TAG_STRING -> {
        return io.github.shiruka.nbt.Tag.createString(base.getAsString());
      }
      case Tag.TAG_LIST -> {
        final var list = io.github.shiruka.nbt.Tag.createList();
        final var originalList = (ListTag) base;
        for (final var entry : originalList) {
          list.add(Converter.convertTag(entry));
        }
        return list;
      }
      case Tag.TAG_COMPOUND -> {
        final var originalCompound = (CompoundTag) base;
        final var compound = io.github.shiruka.nbt.Tag.createCompound();
        for (final var key : originalCompound.getAllKeys()) {
          compound.set(
            key,
            Converter.convertTag(
              Objects.requireNonNull(originalCompound.get(key), "Something went wrong!")
            )
          );
        }
        return compound;
      }
      default -> throw new IllegalArgumentException("Invalid tag type " + base.getId());
    }
  }
}
