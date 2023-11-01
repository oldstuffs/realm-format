package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public interface RealmFormatSerializer {
    @NotNull
    RealmFormatWorld deserialize(
        @NotNull DataInputStream input,
        @NotNull RealmFormatPropertyMap properties
    ) throws IOException;

    void serialize(@NotNull DataOutputStream output, @NotNull RealmFormatWorld world)
        throws IOException;
}
