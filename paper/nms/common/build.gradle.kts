dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  compileOnly(project(":format"))

  compileOnly(dep("paper"))
}
