subprojects {
  dependencies {
    compileOnly(project(":format"))
    compileOnly(project(":modifier:core"))
    compileOnly(project(":paper:api"))

    compileOnly(rootProject.libs.nbt)
  }
}
