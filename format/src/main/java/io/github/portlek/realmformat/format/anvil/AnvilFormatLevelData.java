package io.github.portlek.realmformat.format.anvil;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Builder
@ToString
@EqualsAndHashCode
final class AnvilFormatLevelData {

    private final byte version;

    @NotNull
    private final Map<String, String> gameRules;

    private final int spawnX;

    private final int spawnY;

    private final int spawnZ;
}
