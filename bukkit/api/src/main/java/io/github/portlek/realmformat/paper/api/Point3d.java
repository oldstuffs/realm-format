package io.github.portlek.realmformat.paper.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class Point3d {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
}
