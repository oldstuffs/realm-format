package io.github.portlek.realmformat.paper.nms.v1_19_R3;

import io.github.portlek.realmformat.paper.nms.NmsBackend;
import net.minecraft.SharedConstants;

public final class NmsBackendV1_19_R3 implements NmsBackend {

    @Override
    public int dataVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }
}
