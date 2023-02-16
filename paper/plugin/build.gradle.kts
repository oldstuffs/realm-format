plugins { alias(libs.plugins.run.paper) }

dependencies {
  implementation(project(":format"))
  implementation(project(":modifier:core"))
  implementation(project(":paper:api"))
  implementation(project(":paper:nms:common"))

  compileOnly(project(":paper:nms:v1_18_R2"))
  compileOnly(project(":paper:nms:v1_19_R2"))
  implementation(project(":paper:nms:v1_8_R3"))
  implementation(project(":paper:nms:v1_18_R2", "reobf"))
  implementation(project(":paper:nms:v1_19_R2", "reobf"))

  implementation(libs.smol)

  compileOnly(libs.paper)

  smol(libs.configurate.yaml)
  smol(libs.adventure.configurate) { isTransitive = false }
  smol(libs.terminable)
  smol(libs.task.common)
  smol(libs.task.bukkit)
  smol(libs.event.common)
  smol(libs.event.paper)
  smol(libs.versionmatched)
  smol(libs.cloud.core)
  smol(libs.cloud.annotations)
  smol(libs.cloud.paper)
  smol(libs.cloud.brigadier)
  smol(libs.cloud.tasks)
  smol(libs.cloud.minecraft.extras) { isTransitive = false }
  smol(libs.mariadb)
  smol(libs.mongodb)
  smol(libs.commonsio)
  smol(libs.commonslang)
  smol(libs.nbt)
  smol(libs.zstd)
}

tasks {
  processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(project.the<SourceSetContainer>()["main"].resources.srcDirs) {
      expand("pluginVersion" to project.version)
      include("plugin.yml")
    }
  }

  runServer {
    jvmArgs(
      "-javaagent:" + project(":modifier:agent").tasks["jar"].outputs.files.first().path,
    )
    minecraftVersion("1.19.3")
  }
}
