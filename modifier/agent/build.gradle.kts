dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  api(project(":modifier:core"))

  implementation(dep("javassist"))
  implementation(dep("snakeyaml"))
}

tasks {
  shadowJar { archiveVersion.set("") }

  jar {
    archiveVersion.set("")
    manifest { attributes["Premain-Class"] = "io.github.portlek.realmformat.modifier.Transformer" }
  }
}
