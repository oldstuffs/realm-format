dependencies {
  compileOnly(project(":modifier:realm-format-modifier-core"))
  compileOnly(project(":realm-format-format"))

  compileOnly(libs.spigot)
  compileOnly(libs.configurate.core)
  compileOnly(libs.configurate.yaml)
  compileOnly(libs.pf4j)
}
