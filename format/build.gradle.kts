dependencies {
  compileOnly(libs.zstd)
  compileOnly(libs.fastutil)
  compileOnly(libs.nbt)
  compileOnly(libs.gson)

  testImplementation(libs.nbt)
  testImplementation(libs.zstd)
  testImplementation(libs.fastutil)
  testImplementation(libs.guava)
  testImplementation(libs.commonslang)
}
