plugins { alias(libs.plugins.paperweight) }

dependencies {
  compileOnly(project(":paper:nms:realm-format-paper-nms-common"))

  paperweight { paperDevBundle("1.19.4-R0.1-SNAPSHOT") }
}

tasks { build { dependsOn("reobfJar") } }

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}
