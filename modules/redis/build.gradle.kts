tasks {
  jar {
    manifest { attributes("Plugin-Class" to "${project.group}.realmformat.modules.redis.RedisModule") }
  }
}
