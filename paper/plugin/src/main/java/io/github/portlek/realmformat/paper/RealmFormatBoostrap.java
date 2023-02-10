package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.paper.internal.Dependencies;
import org.bukkit.plugin.java.JavaPlugin;

public final class RealmFormatBoostrap extends JavaPlugin {

  @Override
  public void onLoad() {
    this.getLogger().info("Downloading dependencies...");
    Dependencies.load(this.getDataFolder().toPath().resolve("libs"));
    this.getLogger().info("Dependencies have been downloaded!");
    RealmFormatPlugin.initialize(this);
  }

  @Override
  public void onDisable() {
    RealmFormatPlugin.get().onDisable();
  }

  @Override
  public void onEnable() {
    RealmFormatPlugin.get().onEnable();
  }
}
