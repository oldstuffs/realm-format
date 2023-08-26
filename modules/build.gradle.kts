import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

subprojects {
  dependencies {
    compileOnly(project(":bukkit:realm-format-bukkit-api"))

    compileOnly(rootProject.libs.pf4j)
    compileOnly(rootProject.libs.terminable)
    compileOnly(rootProject.libs.configurate.core)
    compileOnly(rootProject.libs.configurate.yaml)
    compileOnly(rootProject.libs.spigot)
    compileOnly(rootProject.libs.log4j2.api)
    compileOnly(rootProject.libs.event.common)
    compileOnly(rootProject.libs.event.bukkit)
  }

  tasks {
    jar {
      archiveBaseName.set(archiveBaseName.get().replace("realm-format-modules-", ""))
      manifest {
        attributes(
          "Plugin-Id" to project.name.replace("realm-format-modules-", ""),
          "Plugin-Version" to project.version,
          "Plugin-Provider" to "portlek",
        )
      }
    }

    withType<ShadowJar> {
      archiveBaseName.set(archiveBaseName.get().replace("realm-format-modules-", ""))
    }

    val copy = register("copy-to-modules") {
      doLast {
        withType<ShadowJar> {
          val file = outputs.files.files.first()
          file.copyTo(
            project(":bukkit:realm-format-bukkit-plugin").projectDir.resolve("run").resolve("plugins")
              .resolve("RealmFormat").resolve("modules").resolve(file.name),
            overwrite = true,
          )
        }
      }
    }

    build {
      finalizedBy(copy)
    }
  }
}
