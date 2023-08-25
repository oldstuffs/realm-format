subprojects {
  dependencies {
    compileOnly(project(":realm-format-format"))
    compileOnly(project(":modifier:realm-format-modifier-core"))
    compileOnly(project(":bukkit:realm-format-bukkit-api"))

    compileOnly(rootProject.libs.nbt)
  }
}
