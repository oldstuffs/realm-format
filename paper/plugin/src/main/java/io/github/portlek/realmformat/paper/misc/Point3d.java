package io.github.portlek.realmformat.paper.misc;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@ConfigSerializable
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Point3d {

  @Setting
  float pitch;

  @Setting
  double x;

  @Setting
  double y;

  @Setting
  float yaw;

  @Setting
  double z;

  private Point3d(@NotNull final Builder builder) {
    this.x = builder.x;
    this.y = builder.y;
    this.z = builder.z;
    this.yaw = builder.yaw;
    this.pitch = builder.pitch;
  }

  @NotNull
  public static Builder builder() {
    return new Builder();
  }

  @NotNull
  public Point3d.Builder toBuilder() {
    return new Builder(this);
  }

  @Setter
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Builder {

    float pitch;

    double x;

    double y;

    float yaw;

    double z;

    private Builder(@NotNull final Point3d point3d) {
      this.x = point3d.x;
      this.y = point3d.y;
      this.z = point3d.z;
      this.yaw = point3d.yaw;
      this.pitch = point3d.pitch;
    }

    @NotNull
    public Point3d build() {
      return new Point3d(this);
    }
  }
}
