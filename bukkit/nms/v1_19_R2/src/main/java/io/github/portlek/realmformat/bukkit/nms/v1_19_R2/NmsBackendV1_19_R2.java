package io.github.portlek.realmformat.bukkit.nms.v1_19_R2;

import io.github.portlek.realmformat.bukkit.nms.NmsBackend;
import net.minecraft.SharedConstants;

public final class NmsBackendV1_19_R2 implements NmsBackend {

    @Override
    public int dataVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }
}
