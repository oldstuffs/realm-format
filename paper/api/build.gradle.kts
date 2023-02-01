dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  api(project(":modifier:core"))
  api(project(":format"))

  compileOnly(dep("paper"))
  compileOnly(dep("task-common"))
  compileOnly(dep("task-bukkit"))
}
