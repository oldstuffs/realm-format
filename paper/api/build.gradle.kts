dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  api(project(":modifier:core"))

  compileOnly(dep("paper"))
}
