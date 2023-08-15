import com.diffplug.spotless.LineEnding
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  java
  `maven-publish`
  signing
  alias(libs.plugins.spotless)
  alias(libs.plugins.nexus)
  alias(libs.plugins.shadow) apply false
  alias(libs.plugins.paperweight) apply false
  alias(libs.plugins.run.paper) apply false
}

val shadePackage = property("shade.package")
val signRequired = !property("dev").toString().toBoolean()
val relocations =
  property("relocations")
    .toString()
    .trim()
    .replace(" ", "")
    .split(",")
    .filter { it.isNotEmpty() }
    .filter { it.isNotBlank() }

defaultTasks("build")

repositories { mavenCentral() }

allprojects { group = "io.github.portlek" }

subprojects {
  apply<JavaPlugin>()

  val shadowEnabled = findProperty("shadow.enabled")?.toString().toBoolean()
  val shadowRelocation = findProperty("shadow.relocation")?.toString().toBoolean()
  val mavenPublish = findProperty("maven.publish")?.toString().toBoolean()

  java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  tasks {
    test {
      useJUnitPlatform()
      testLogging { showStandardStreams = true }
    }

    compileJava { options.encoding = Charsets.UTF_8.name() }

    build { dependsOn(jar) }
  }

  if (shadowEnabled) {
    apply<ShadowPlugin>()

    tasks {
      withType<ShadowJar> {
        dependsOn(jar)

        mergeServiceFiles()

        archiveClassifier.set("")

        if (shadowRelocation) {
          relocations.forEach { relocate(it, "$shadePackage.$it") }
        }
      }

      build { dependsOn("shadowJar") }
    }
  }

  if (mavenPublish) {
    apply<MavenPublishPlugin>()
    apply<SigningPlugin>()

    tasks {
      javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).tags("todo")
      }

      val javadocJar by
        creating(Jar::class) {
          dependsOn("javadoc")
          archiveClassifier.set("javadoc")
          from(javadoc)
        }

      val sourcesJar by
        creating(Jar::class) {
          dependsOn("classes")
          archiveClassifier.set("sources")
          from(sourceSets["main"].allSource)
        }

      build {
        dependsOn(javadocJar)
        dependsOn(sourcesJar)
      }
    }

    publishing {
      publications {
        val publication =
          create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
              name.set("RealmFormatApi")
              description.set("A single-file world format plugin for Minecraft")
              url.set("https://github.com/portlek/")
              licenses {
                license {
                  name.set("MIT License")
                  url.set("https://mit-license.org/license.txt")
                }
              }
              developers {
                developer {
                  id.set("portlek")
                  name.set("Hasan Demirtaş")
                  email.set("utsukushihito@outlook.com")
                }
              }
              scm {
                connection.set("scm:git:git://github.com/portlek/realm-format.git")
                developerConnection.set("scm:git:ssh://github.com/portlek/realm-format.git")
                url.set("https://github.com/portlek/realm-format")
              }
            }
          }

        signing {
          isRequired = signRequired
          if (isRequired) {
            useGpgCmd()
            sign(publication)
          }
        }
      }
    }
  }

  repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://papermc.io/repo/repository/maven-public/")
    mavenLocal()
  }

  dependencies {
    compileOnly(rootProject.libs.lombok)
    compileOnly(rootProject.libs.annotations)

    annotationProcessor(rootProject.libs.lombok)
    annotationProcessor(rootProject.libs.annotations)

    testImplementation(rootProject.libs.junit.api)
    testRuntimeOnly(rootProject.libs.junit.engine)

    testAnnotationProcessor(rootProject.libs.lombok)
    testAnnotationProcessor(rootProject.libs.annotations)
  }
}

nexusPublishing { repositories { sonatype() } }

spotless {
  lineEndings = LineEnding.UNIX

  val prettierConfig =
    mapOf(
      "prettier" to "2.8.8",
      "prettier-plugin-java" to "2.2.0",
    )

  format("encoding") {
    target("*.*")
    targetExclude("modifier/agent/src/main/resources/realm-format-modifier-core.txt")
    encoding("UTF-8")
    endWithNewline()
    trimTrailingWhitespace()
  }

  yaml {
    target(
      "**/src/main/resources/*.yaml",
      "**/src/main/resources/*.yml",
      ".github/**/*.yml",
      ".github/**/*.yaml",
    )
    endWithNewline()
    trimTrailingWhitespace()
    val jackson = jackson()
    jackson.yamlFeature("LITERAL_BLOCK_STYLE", true)
    jackson.yamlFeature("MINIMIZE_QUOTES", true)
    jackson.yamlFeature("SPLIT_LINES", false)
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    indentWithSpaces(2)
    endWithNewline()
    trimTrailingWhitespace()
    ktlint()
  }

  java {
    target("**/src/**/java/**/*.java")
    importOrder()
    removeUnusedImports()
    indentWithSpaces(2)
    endWithNewline()
    trimTrailingWhitespace()
    prettier(prettierConfig)
      .config(
        mapOf("parser" to "java", "tabWidth" to 2, "useTabs" to false, "printWidth" to 100),
      )
  }
}
