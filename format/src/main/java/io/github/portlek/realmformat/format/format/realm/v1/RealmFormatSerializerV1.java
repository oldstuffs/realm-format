package io.github.portlek.realmformat.format.format.realm.v1;

import com.github.luben.zstd.Zstd;
import io.github.portlek.realmformat.format.format.realm.RealmFormatSerializer;
import io.github.portlek.realmformat.format.format.realm.RealmFormatWorld;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RealmFormatSerializerV1 implements RealmFormatSerializer {

  public static final RealmFormatSerializerV1 INSTANCE = new RealmFormatSerializerV1();

  private static byte@NotNull[] readCompressed(@NotNull final DataInputStream stream)
    throws IOException {
    final var compressedLength = stream.readInt();
    final var resultLength = stream.readInt();
    final var compressed = new byte[compressedLength];
    final var result = new byte[resultLength];
    stream.read(compressed);
    Zstd.decompress(result, compressed);
    return result;
  }

  @NotNull
  @Override
  public RealmFormatWorld deserialize(@NotNull final DataInputStream input) throws IOException {
    final var worldVersion = input.readByte();
    return RealmFormatWorldV1.builder().worldVersion(worldVersion).build();
  }

  @Override
  public void serialize(
    @NotNull final DataOutputStream output,
    @NotNull final RealmFormatWorld world
  ) throws IOException {
    output.writeByte(world.worldVersion());
  }
}
