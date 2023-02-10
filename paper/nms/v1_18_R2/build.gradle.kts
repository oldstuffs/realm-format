plugins { id("io.papermc.paperweight.userdev") }

dependencies {
  implementation(project(":paper:nms:common"))

  paperweight { paperDevBundle("1.18.2-R0.1-SNAPSHOT") }
}

tasks { build { dependsOn("reobfJar") } }
