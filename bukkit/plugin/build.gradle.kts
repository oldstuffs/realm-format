plugins { alias(libs.plugins.run.paper) }

dependencies {
  implementation(project(":realm-format-format"))
  implementation(project(":modifier:realm-format-modifier-core"))
  implementation(project(":bukkit:realm-format-bukkit-api"))
  implementation(project(":bukkit:nms:realm-format-bukkit-nms-common"))

  compileOnly(project(":bukkit:nms:realm-format-bukkit-nms-v1_19_R3"))
  implementation(project(":bukkit:nms:realm-format-bukkit-nms-v1_19_R3", "reobf"))

  compileOnly(libs.spigot)

  implementation(libs.commonsio)
  implementation(libs.pf4j)
  implementation(libs.zstd)
  implementation(libs.nbt)
  implementation(libs.event.common)
  implementation(libs.event.bukkit)
  implementation(libs.task.common)
  implementation(libs.task.bukkit)
  implementation(libs.versionmatched)
  implementation(libs.configurate.core)
  implementation(libs.configurate.yaml)
  implementation(libs.cloud.core)
  implementation(libs.cloud.annotations)
  implementation(libs.cloud.paper)
  implementation(libs.cloud.brigadier)
  implementation(libs.cloud.tasks)
}

tasks {
  processResources {
    filesMatching("plugin.yml") {
      expand(project.properties)
    }
  }

  runServer {
    jvmArgs(
      "-javaagent:" + project(":modifier:agent").tasks.jar.get().outputs.files.first().path,
    )
    minecraftVersion("1.19.3")
  }
}
