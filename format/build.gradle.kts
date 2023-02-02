dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  compileOnlyApi(dep("terminable"))
  compileOnlyApi(dep("task-common"))
  compileOnlyApi(dep("nbt"))
  compileOnlyApi(dep("zstd"))
}
