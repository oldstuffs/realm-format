package io.github.portlek.realmformat.format.realm.upgrader;

import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.upgrader.v1_11.RealmFormatWorldUpgradeV1_11;
import io.github.portlek.realmformat.format.realm.upgrader.v1_13.RealmFormatWorldUpgradeV1_13;
import io.github.portlek.realmformat.format.realm.upgrader.v1_14.RealmFormatWorldUpgradeV1_14;
import io.github.portlek.realmformat.format.realm.upgrader.v1_16.RealmFormatWorldUpgradeV1_16;
import io.github.portlek.realmformat.format.realm.upgrader.v1_17.RealmFormatWorldUpgradeV1_17;
import io.github.portlek.realmformat.format.realm.upgrader.v1_18.RealmFormatWorldUpgradeV1_18;
import io.github.portlek.realmformat.format.realm.upgrader.v1_9.RealmFormatWorldUpgradeV1_9;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class that contains upgrade methods for {@link RealmFormatWorld}.
 * <p>
 * Run {@link #initiate()} method before using other methods.
 */
@Log
@UtilityClass
public class RealmFormatWorldUpgrades {

  /**
   * The upgrade list.
   */
  private final Map<Byte, RealmFormatWorldUpgrade> UPGRADES = new HashMap<>(
    Map.of(
      (byte) 0x02,
      new RealmFormatWorldUpgradeV1_9(),
      (byte) 0x03,
      new RealmFormatWorldUpgradeV1_11(),
      (byte) 0x04,
      new RealmFormatWorldUpgradeV1_13(),
      (byte) 0x05,
      new RealmFormatWorldUpgradeV1_14(),
      (byte) 0x06,
      new RealmFormatWorldUpgradeV1_16(),
      (byte) 0x07,
      new RealmFormatWorldUpgradeV1_17(),
      (byte) 0x08,
      new RealmFormatWorldUpgradeV1_18()
    )
  );

  /**
   * Tries to upgrade the specified world to {@code to}.
   *
   * @param world The world to upgrade.
   * @param version The version upgrade to.
   */
  public void apply(@NotNull final RealmFormatWorld world, final byte version) {
    for (var ver = (byte) (world.worldVersion() + 1); ver <= version; ver++) {
      final var upgrade = RealmFormatWorldUpgrades.get(ver);
      if (upgrade == null) {
        RealmFormatWorldUpgrades.log.log(
          Level.WARNING,
          "Missing world upgrades for version {0}. World will not be upgraded.",
          ver
        );
        continue;
      }
      upgrade.apply(world);
    }
  }

  /**
   * Obtains the {@link RealmFormatWorldUpgrade} instance by version.
   *
   * @param version The version to retrieve the upgrade instance.
   *
   * @return found {@link RealmFormatWorldUpgrade} instance.
   */
  @Nullable
  public RealmFormatWorldUpgrade get(final byte version) {
    return RealmFormatWorldUpgrades.UPGRADES.get(version);
  }

  /**
   * ignored.
   */
  public void initiate() {
    // ignored
  }

  /**
   * Registers a {@link RealmFormatWorldUpgrade} using specified version.
   *
   * @param version The version to register.
   * @param upgrade The upgrade to register.
   */
  public void register(final byte version, @NotNull final RealmFormatWorldUpgrade upgrade) {
    RealmFormatWorldUpgrades.UPGRADES.put(version, upgrade);
  }
}
