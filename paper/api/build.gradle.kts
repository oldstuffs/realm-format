dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  compileOnly(project(":modifier:core"))

  compileOnly(dep("paper"))
}
