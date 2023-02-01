dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  api(project(":format"))

  compileOnly(dep("paper"))
}
