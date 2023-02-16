plugins { alias(libs.plugins.paperweight) }

dependencies {
  compileOnly(project(":paper:nms:common"))

  paperweight { paperDevBundle("1.19.3-R0.1-SNAPSHOT") }
}

tasks { build { dependsOn("reobfJar") } }
