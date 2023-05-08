plugins { alias(libs.plugins.run.paper) }

dependencies {
  implementation(project(":realm-format-format"))
  implementation(project(":modifier:realm-format-modifier-core"))
  implementation(project(":paper:realm-format-paper-api"))
  implementation(project(":paper:nms:realm-format-paper-nms-common"))

  compileOnly(project(":paper:nms:realm-format-paper-nms-v1_18_R2"))
  compileOnly(project(":paper:nms:realm-format-paper-nms-v1_19_R3"))
  implementation(project(":paper:nms:realm-format-paper-nms-v1_8_R3"))
  implementation(project(":paper:nms:realm-format-paper-nms-v1_18_R2", "reobf"))
  implementation(project(":paper:nms:realm-format-paper-nms-v1_19_R3", "reobf"))

  compileOnly(libs.paper)

  compileOnly(libs.commonsio)

  implementation(libs.configurate.yaml)
  implementation(libs.adventure.configurate) { isTransitive = false }
  implementation(libs.terminable)
  implementation(libs.task.common)
  implementation(libs.task.bukkit)
  implementation(libs.event.common)
  implementation(libs.event.paper)
  implementation(libs.versionmatched)
  implementation(libs.cloud.core)
  implementation(libs.cloud.annotations)
  implementation(libs.cloud.paper)
  implementation(libs.cloud.brigadier)
  implementation(libs.cloud.tasks)
  implementation(libs.cloud.minecraft.extras) { isTransitive = false }
  implementation(libs.mariadb)
  implementation(libs.mongodb)
  implementation(libs.nbt)
  implementation(libs.zstd)
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
