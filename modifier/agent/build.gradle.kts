import io.papermc.paperweight.util.path
import java.nio.file.Files
import java.nio.file.StandardCopyOption

val coreProject = project(":modifier:core")

dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  api(coreProject)

  implementation(dep("javassist"))
  implementation(dep("snakeyaml"))
}

tasks {
  shadowJar { archiveVersion.set("") }

  processResources {
    dependsOn(coreProject.tasks.shadowJar)
    doFirst {
      val builtCore = coreProject.layout.buildDirectory.path.resolve("libs").resolve("realm-format-modifier-core.jar")
      val destination = project.layout.projectDirectory.path.resolve("src").resolve("main").resolve("resources").resolve("realm-format-modifier-core.txt")
      Files.copy(builtCore, destination, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  jar {
    archiveVersion.set("")
    manifest { attributes["Premain-Class"] = "io.github.portlek.realmformat.modifier.Transformer" }
  }
}
