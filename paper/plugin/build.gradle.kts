dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  implementation(project(":paper:api"))

  compileOnly(dep("paper"))
}
