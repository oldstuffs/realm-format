import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

subprojects {
  dependencies {
    compileOnly(project(":bukkit:realm-format-bukkit-api"))

    compileOnly(rootProject.libs.pf4j)
  }

  tasks {
    jar {
      archiveBaseName.set(archiveBaseName.get().replace("realm-format-modules-", ""))
      archiveVersion.set("")
      manifest {
        attributes(
          "Plugin-Id" to project.name,
          "Plugin-Version" to project.version,
          "Plugin-Provider" to "portlek",
        )
      }
    }

    withType<ShadowJar> {
      archiveBaseName.set(archiveBaseName.get().replace("realm-format-modules-", ""))
      archiveVersion.set("")
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
