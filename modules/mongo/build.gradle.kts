dependencies {
  implementation(libs.mongodb)

  compileOnly(libs.terminable)
  compileOnly(libs.configurate.core)
  compileOnly(libs.configurate.yaml)
  compileOnly(rootProject.libs.spigot)
}

tasks {
  jar {
    manifest { attributes("Plugin-Class" to "${project.group}.realmformat.modules.mongo.MongoModule") }
  }
}
