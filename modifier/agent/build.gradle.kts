dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  api(project(":modifier:core"))

  implementation(dep("javassist"))
}

tasks {
  jar { manifest { attributes["Premain-Class"] = "io.github.portlek.realmformat.modifier.Agent" } }
}
