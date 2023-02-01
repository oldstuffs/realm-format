dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  compileOnlyApi(dep("nbt"))
  compileOnlyApi(dep("zstd"))
}
