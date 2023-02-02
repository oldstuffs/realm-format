dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  compileOnly(dep("nbt"))
  compileOnly(dep("zstd"))
  compileOnly(dep("fastutil"))
}
