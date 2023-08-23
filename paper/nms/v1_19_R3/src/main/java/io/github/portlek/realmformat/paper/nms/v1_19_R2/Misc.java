package io.github.portlek.realmformat.paper.nms.v1_19_R2;

import io.github.portlek.realmformat.format.misc.NibbleArray;
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

@Log4j2
@UtilityClass
class Misc {

    @NotNull
    static DataLayer fromNibbleArray(@NotNull final NibbleArray array) {
        return new DataLayer(array.backing());
    }

    @NotNull
    static Tag fromTag(@NotNull final io.github.shiruka.nbt.Tag tag) {
        try {
            return switch (tag.getType()) {
                case BYTE -> ByteTag.valueOf(tag.asNumber().byteValue());
                case SHORT -> ShortTag.valueOf(tag.asNumber().shortValue());
                case INT -> IntTag.valueOf(tag.asNumber().intValue());
                case LONG -> LongTag.valueOf(tag.asNumber().longValue());
                case FLOAT -> FloatTag.valueOf(tag.asNumber().floatValue());
                case DOUBLE -> DoubleTag.valueOf(tag.asNumber().doubleValue());
                case BYTE_ARRAY -> new ByteArrayTag(tag.asByteArray().primitiveValue());
                case INT_ARRAY -> new IntArrayTag(tag.asIntArray().primitiveValue());
                case LONG_ARRAY -> new LongArrayTag(tag.asLongArray().primitiveValue());
                case STRING -> StringTag.valueOf(tag.asString().value());
                case LIST -> {
                    final ListTag list = new ListTag();
                    tag.asList().stream().map(Misc::fromTag).forEach(list::add);
                    yield list;
                }
                case COMPOUND -> {
                    final CompoundTag compound = new CompoundTag();
                    tag
                        .asCompound()
                        .all()
                        .forEach((key, value) -> compound.put(key, Misc.fromTag(value)));
                    yield compound;
                }
                default -> throw new IllegalArgumentException(
                    "Invalid tag type " + tag.getType().name()
                );
            };
        } catch (final Exception e) {
            Misc.log.error("Failed to convert NBT object:");
            Misc.log.error(tag.toString());
            throw e;
        }
    }

    @NotNull
    static NibbleArray toNibbleArray(@NotNull final DataLayer array) {
        return new NibbleArray(array.getData());
    }

    @NotNull
    static io.github.shiruka.nbt.Tag toTag(@NotNull final Tag tag) {
        return switch (tag.getId()) {
            case Tag.TAG_BYTE -> io.github.shiruka.nbt.Tag.createByte(((ByteTag) tag).getAsByte());
            case Tag.TAG_SHORT -> io.github.shiruka.nbt.Tag.createShort(
                ((ShortTag) tag).getAsShort()
            );
            case Tag.TAG_INT -> io.github.shiruka.nbt.Tag.createInt(((IntTag) tag).getAsInt());
            case Tag.TAG_LONG -> io.github.shiruka.nbt.Tag.createLong(((LongTag) tag).getAsLong());
            case Tag.TAG_FLOAT -> io.github.shiruka.nbt.Tag.createFloat(
                ((FloatTag) tag).getAsFloat()
            );
            case Tag.TAG_DOUBLE -> io.github.shiruka.nbt.Tag.createDouble(
                ((DoubleTag) tag).getAsDouble()
            );
            case Tag.TAG_BYTE_ARRAY -> io.github.shiruka.nbt.Tag.createByteArray(
                ((ByteArrayTag) tag).getAsByteArray()
            );
            case Tag.TAG_INT_ARRAY -> io.github.shiruka.nbt.Tag.createIntArray(
                ((IntArrayTag) tag).getAsIntArray()
            );
            case Tag.TAG_LONG_ARRAY -> io.github.shiruka.nbt.Tag.createLongArray(
                ((LongArrayTag) tag).getAsLongArray()
            );
            case Tag.TAG_STRING -> io.github.shiruka.nbt.Tag.createString(tag.getAsString());
            case Tag.TAG_LIST -> {
                final io.github.shiruka.nbt.ListTag list = io.github.shiruka.nbt.Tag.createList();
                ((ListTag) tag).stream().map(Misc::toTag).forEach(list::add);
                yield list;
            }
            case Tag.TAG_COMPOUND -> {
                final CompoundTag originalCompound = (CompoundTag) tag;
                final io.github.shiruka.nbt.CompoundTag compound =
                    io.github.shiruka.nbt.Tag.createCompound();
                for (final String key : originalCompound.getAllKeys()) {
                    final Tag value = originalCompound.get(key);
                    if (value != null) {
                        compound.set(key, Misc.toTag(value));
                    }
                }
                yield compound;
            }
            default -> throw new IllegalArgumentException("Invalid tag type " + tag.getId());
        };
    }
}
