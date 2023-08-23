plugins { alias(libs.plugins.run.paper) }

dependencies {
  implementation(project(":realm-format-format"))
  implementation(project(":modifier:realm-format-modifier-core"))
  implementation(project(":paper:realm-format-paper-api"))
  implementation(project(":paper:nms:realm-format-paper-nms-common"))

  compileOnly(project(":paper:nms:realm-format-paper-nms-v1_19_R3"))
  implementation(project(":paper:nms:realm-format-paper-nms-v1_19_R3", "reobf"))

  compileOnly(libs.bukkit)

  compileOnly(libs.commonsio)

  implementation(libs.event.common)
  implementation(libs.event.bukkit)

  implementation(libs.task.common)
  implementation(libs.task.bukkit)

  implementation(libs.cloud.core)
  implementation(libs.cloud.annotations)
  implementation(libs.cloud.paper)
  implementation(libs.cloud.brigadier)
  implementation(libs.cloud.tasks)
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
