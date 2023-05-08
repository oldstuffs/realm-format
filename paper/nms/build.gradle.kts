subprojects {
  dependencies {
    compileOnly(project(":realm-format-format"))
    compileOnly(project(":modifier:realm-format-modifier-core"))
    compileOnly(project(":paper:realm-format-paper-api"))

    compileOnly(rootProject.libs.nbt)
    compileOnly(rootProject.libs.terminable)
  }
}
