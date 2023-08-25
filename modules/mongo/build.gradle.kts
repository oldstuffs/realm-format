dependencies {
  implementation(libs.mongodb)

  compileOnly(libs.terminable)
  compileOnly(libs.configurate.core)
  compileOnly(libs.configurate.yaml)
  compileOnly(rootProject.libs.spigot)
  compileOnly(libs.log4j2.api)
}

tasks {
  jar {
    manifest { attributes("Plugin-Class" to "${project.group}.realmformat.modules.mongo.MongoModule") }
  }
}
