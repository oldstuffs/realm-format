dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  compileOnly(dep("zstd"))
  compileOnly(dep("fastutil"))
  compileOnly(dep("nbt"))

  testImplementation(dep("nbt"))
  testImplementation(dep("zstd"))
  testImplementation(dep("fastutil"))
  testImplementation(dep("guava"))
  testImplementation(dep("commonslang"))
}
