dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  compileOnly(dep("nbt"))
  compileOnly(dep("zstd"))

  testImplementation(dep("nbt"))
  testImplementation(dep("zstd"))
  testImplementation(dep("fastutil"))
  testImplementation(dep("guava"))
}
