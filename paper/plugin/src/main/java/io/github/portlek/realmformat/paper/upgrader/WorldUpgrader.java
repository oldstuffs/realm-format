package io.github.portlek.realmformat.paper.upgrader;

import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.portlek.realmformat.paper.misc.Services;
import io.github.portlek.realmformat.paper.nms.RealmNmsBackend;
import io.github.portlek.realmformat.paper.upgrader.v1_11.WorldUpgrade1_11;
import io.github.portlek.realmformat.paper.upgrader.v1_13.WorldUpgrade1_13;
import io.github.portlek.realmformat.paper.upgrader.v1_14.WorldUpgrade1_14;
import io.github.portlek.realmformat.paper.upgrader.v1_16.WorldUpgrade1_16;
import io.github.portlek.realmformat.paper.upgrader.v1_17.WorldUpgrade1_17;
import io.github.portlek.realmformat.paper.upgrader.v1_18.WorldUpgrade1_18;
import io.github.portlek.realmformat.paper.upgrader.v1_9.WorldUpgrade1_9;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
@UtilityClass
public class WorldUpgrader {

  private final RealmNmsBackend nms = Services.load(RealmNmsBackend.class);

  private final Map<Byte, Upgrade> upgrades = new HashMap<>();

  static {
    WorldUpgrader.upgrades.put((byte) 2, new WorldUpgrade1_9());
    WorldUpgrader.upgrades.put((byte) 3, new WorldUpgrade1_11());
    WorldUpgrader.upgrades.put((byte) 4, new WorldUpgrade1_13());
    WorldUpgrader.upgrades.put((byte) 5, new WorldUpgrade1_14());
    WorldUpgrader.upgrades.put((byte) 6, new WorldUpgrade1_16());
    WorldUpgrader.upgrades.put((byte) 7, new WorldUpgrade1_17());
    WorldUpgrader.upgrades.put((byte) 8, new WorldUpgrade1_18());
  }

  public void upgradeWorld(@NotNull final RealmWorld world) {
    final byte serverVersion = WorldUpgrader.nms.worldVersion();
    for (byte ver = (byte) (world.version() + 1); ver <= serverVersion; ver++) {
      final Upgrade upgrade = WorldUpgrader.upgrades.get(ver);
      if (upgrade == null) {
        WorldUpgrader.log.error(
          "Missing world upgrader for version " + ver + ". World will not be upgraded."
        );
        continue;
      }
      upgrade.upgrade(world);
    }
    world.version(serverVersion);
  }
}
