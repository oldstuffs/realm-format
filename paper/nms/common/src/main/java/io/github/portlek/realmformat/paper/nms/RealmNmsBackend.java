package io.github.portlek.realmformat.paper.nms;

import io.github.portlek.realmformat.format.old.realm.RealmWorld;
import java.io.IOException;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RealmNmsBackend extends RealmWorldSource {
  void generateWorld(@NotNull RealmWorld world);

  byte worldVersion();

  @Nullable
  Object injectDefaultWorlds();

  @Nullable
  RealmWorld realmWorld(@NotNull World world);

  void defaultWorlds(
    @Nullable RealmWorld normalWorld,
    @Nullable RealmWorld netherWorld,
    @Nullable RealmWorld endWorld
  ) throws IOException;
}
