import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.portlek.smol.SmolPlugin
import io.github.portlek.smol.tasks.SmolJar

plugins {
  java
  `java-library`
  `maven-publish`
  signing
  id("com.diffplug.spotless") version "6.14.1"
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
  id("com.github.johnrengelman.shadow") version "7.1.2" apply false
  id("io.github.portlek.smol-plugin-gradle") version "0.2.2-SNAPSHOT" apply false
  id("io.papermc.paperweight.userdev") version "1.5.0" apply false
  id("xyz.jpenilla.run-paper") version "2.0.0" apply false
}

val spotlessApply = property("spotless.apply").toString().toBoolean()
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

repositories { mavenCentral() }

if (spotlessApply) {
  configure<SpotlessExtension> {
    lineEndings = LineEnding.UNIX
    isEnforceCheck = false

    format("encoding") {
      target("modifier/agent/src/main/resources/**/*.*", ".run/*.*")
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
          ".github/**/*.yaml")
      endWithNewline()
      trimTrailingWhitespace()
      jackson()
    }

    kotlinGradle {
      target("**/*.gradle.kts")
      indentWithSpaces(2)
      endWithNewline()
      trimTrailingWhitespace()
      ktfmt()
    }

    java {
      target("**/src/**/java/**/*.java")
      importOrder()
      removeUnusedImports()
      indentWithSpaces(2)
      endWithNewline()
      trimTrailingWhitespace()
      prettier(mapOf("prettier" to "2.8.3", "prettier-plugin-java" to "2.0.0"))
          .config(
              mapOf("parser" to "java", "tabWidth" to 2, "useTabs" to false, "printWidth" to 100))
    }
  }
}

allprojects { group = "io.github.portlek" }

subprojects {
  apply<JavaPlugin>()
  apply<JavaLibraryPlugin>()

  val projectName = property("project.name").toString()

  java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

  tasks {
    test {
      useJUnitPlatform()
      testLogging { showStandardStreams = true }
    }

    compileJava { options.encoding = Charsets.UTF_8.name() }

    jar { archiveBaseName.set(projectName) }

    build { dependsOn(jar) }

    if (findProperty("shadow.enabled")?.toString().toBoolean()) {
      apply<ShadowPlugin>()

      withType<ShadowJar> {
        mergeServiceFiles()

        archiveBaseName.set(projectName)
        archiveClassifier.set("")

        if (findProperty("shadow.relocation")?.toString().toBoolean()) {
          relocations.forEach { relocate(it, "$shadePackage.$it") }
        }
      }

      build { dependsOn("shadowJar") }
    }

    if (findProperty("smol.enabled")?.toString().toBoolean()) {
      apply<SmolPlugin>()

      withType<SmolJar> {
        if (findProperty("smol.relocation")?.toString().toBoolean()) {
          relocations.forEach { relocate(it, "$shadePackage.$it") }
        }
      }
    }
  }

  if (findProperty("maven.publish")?.toString().toBoolean()) {
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
            archiveBaseName.set(projectName)
            archiveVersion.set(project.version.toString())
            from(javadoc)
          }

      val sourcesJar by
          creating(Jar::class) {
            dependsOn("classes")
            archiveClassifier.set("sources")
            archiveBaseName.set(projectName)
            archiveVersion.set(project.version.toString())
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
              groupId = project.group.toString()
              artifactId = projectName
              version = project.version.toString()

              from(components["java"])
              artifact(tasks["sourcesJar"])
              artifact(tasks["javadocJar"])
              pom {
                name.set("Event")
                description.set("A builder-like event library for Paper/Velocity.")
                url.set("https://infumia.com.tr/")
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
                  connection.set("scm:git:git://github.com/infumia/event.git")
                  developerConnection.set("scm:git:ssh://github.com/infumia/event.git")
                  url.set("https://github.com/infumia/event")
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
  }

  dependencies {
    fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

    compileOnly(dep("lombok"))
    compileOnly(dep("annotations"))

    annotationProcessor(dep("lombok"))
    annotationProcessor(dep("annotations"))

    testImplementation(dep("junit.api"))
    testRuntimeOnly(dep("junit.engine"))

    testAnnotationProcessor(dep("lombok"))
    testAnnotationProcessor(dep("annotations"))
  }
}

nexusPublishing { repositories { sonatype() } }
