dependencies { api(project(":common")) }

tasks {
  shadowJar { archiveVersion.set("") }

  jar { archiveVersion.set("") }
}
