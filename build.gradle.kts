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
  alias(libs.plugins.spotless)
  alias(libs.plugins.nexus)
  alias(libs.plugins.shadow)
  alias(libs.plugins.smol) apply false
  alias(libs.plugins.paperweight) apply false
  alias(libs.plugins.run.paper) apply false
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
  tasks {
    build {
      dependsOn(spotlessApply)
    }
  }

  configure<SpotlessExtension> {
    lineEndings = LineEnding.UNIX
    isEnforceCheck = false

    val prettierConfig =
      mapOf(
        "prettier" to "latest",
        "prettier-plugin-java" to "latest",
        "@prettier/plugin-xml" to "latest",
      )

    format("encoding") {
      target("modifier/agent/src/main/resources/**/*.*", "/.editorconfig")
      targetExclude("modifier/agent/src/main/resources/realm-format-modifier-core.txt")
      encoding("UTF-8")
      endWithNewline()
      trimTrailingWhitespace()
    }

    format("xml") {
      target(".run/*.xml")
      encoding("UTF-8")
      endWithNewline()
      trimTrailingWhitespace()
      prettier(prettierConfig)
        .config(
          mapOf(
            "printWidth" to 100,
            "xmlSelfClosingSpace" to false,
            "xmlWhitespaceSensitivity" to "ignore",
          ),
        )
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
      jackson()
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

    if (hasProperty("shadow.enabled")) {
      apply<ShadowPlugin>()

      val shadowJar = withType<ShadowJar> {
        dependsOn(jar)

        mergeServiceFiles()

        archiveBaseName.set(projectName)
        archiveClassifier.set("")

        if (hasProperty("shadow.relocation")) {
          relocations.forEach { relocate(it, "$shadePackage.$it") }
        }
      }

      build { dependsOn(shadowJar) }
    }

    if (hasProperty("smol.enabled")) {
      apply<SmolPlugin>()

      val smolJar = withType<SmolJar> {
        if (hasProperty("smol.relocation")) {
          relocations.forEach { relocate(it, "$shadePackage.$it") }
        }
      }

      build {
        dependsOn(smolJar)
      }
    }
  }

  if (hasProperty("maven.publish")) {
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
