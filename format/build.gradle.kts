dependencies {
  compileOnly(libs.zstd)
  compileOnly(libs.nbt)
  compileOnly(libs.gson)

  testImplementation(libs.nbt)
  testImplementation(libs.zstd)
  testImplementation(libs.guava)
  testImplementation(libs.commonslang)
}
