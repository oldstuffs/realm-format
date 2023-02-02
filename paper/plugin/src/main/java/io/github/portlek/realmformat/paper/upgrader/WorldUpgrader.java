package io.github.portlek.realmformat.paper.upgrader;

import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.portlek.realmformat.paper.misc.Services;
import io.github.portlek.realmformat.paper.nms.RealmNmsBackend;
import io.github.portlek.realmformat.paper.upgrader.v117.WorldUpgrade1_17;
import io.github.portlek.realmformat.paper.upgrader.v1_14.WorldUpgrade1_14;
import io.github.portlek.realmformat.paper.upgrader.v1_16.WorldUpgrade1_16;
import io.github.portlek.realmformat.paper.upgrader.v1_18.WorldUpgrade1_18;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
@UtilityClass
public class WorldUpgrader {

  private final Map<Byte, Upgrade> upgrades = new HashMap<>();

  static {
    WorldUpgrader.upgrades.put((byte) 0x04, new WorldUpgrade1_14());
    // Todo we need a 1_14_WorldUpgrade class as well for 0x05
    WorldUpgrader.upgrades.put((byte) 0x06, new WorldUpgrade1_16());
    WorldUpgrader.upgrades.put((byte) 0x07, new WorldUpgrade1_17());
    WorldUpgrader.upgrades.put((byte) 0x08, new WorldUpgrade1_18());
  }

  private final RealmNmsBackend nms = Services.load(RealmNmsBackend.class);
  public void upgradeWorld(@NotNull final RealmWorld world) {
    final byte serverVersion = nms.worldVersion();
    for (byte ver = (byte) (world.version() + 1); ver <= serverVersion; ver++) {
      final Upgrade upgrade = WorldUpgrader.upgrades.get(ver);
      if (upgrade == null) {
        log.error("Missing world upgrader for version " + ver + ". World will not be upgraded.");
        continue;
      }
      upgrade.upgrade(world);
    }
    world.version(serverVersion);
  }
}
