dependencies {
  implementation(libs.mongodb)
}

tasks {
  jar {
    manifest { attributes("Plugin-Class" to "${project.group}.realmformat.modules.mongo.MongoModule") }
  }
}
