plugins { alias(libs.plugins.paperweight) }

dependencies {
  implementation(project(":paper:nms:realm-format-paper-nms-common"))

  paperweight { paperDevBundle("1.18.2-R0.1-SNAPSHOT") }
}

tasks { build { dependsOn("reobfJar") } }
