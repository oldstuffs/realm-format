import io.papermc.paperweight.util.path
import java.nio.file.Files
import java.nio.file.StandardCopyOption

val coreProject = project(":modifier:core")

dependencies {
  compileOnly(project(":modifier:core"))

  implementation(libs.javassist)
  implementation(libs.snakeyaml)
}

tasks {
  shadowJar { archiveVersion.set("") }

  processResources {
    dependsOn(coreProject.tasks.jar)
    doFirst {
      val builtCore = project(":modifier:agent").tasks["jar"].outputs.files.first().toPath()
      val destination = project.layout.projectDirectory.path.resolve("src").resolve("main").resolve("resources").resolve("realm-format-modifier-core.txt")
      Files.copy(builtCore, destination, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  jar {
    archiveVersion.set("")
    manifest { attributes["Premain-Class"] = "io.github.portlek.realmformat.modifier.Transformer" }
  }
}
