subprojects {
  dependencies {
    compileOnly(project(":modifier:core"))
    compileOnly(project(":paper:api"))
  }
}
