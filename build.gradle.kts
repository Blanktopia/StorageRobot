@file:Suppress("SpellCheckingInspection")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()

    maven("https://repo.purpurmc.org/snapshots")
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }

    // bStats
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }

    // CommandAPI
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }

    mavenLocal()
}

dependencies {
    compileOnly(kotlin("stdlib"))
    implementation("com.charleskorn.kaml:kaml:0.104.0")

    // Paper
    compileOnly("org.purpurmc.purpur", "purpur-api", "1.21.5-R0.1-SNAPSHOT")

    // CommandAPI
    compileOnly("dev.jorel:commandapi-bukkit-core:10.0.1")
    compileOnly("dev.jorel:commandapi-bukkit-kotlin:10.0.1")

    // bStats
    implementation("org.bstats", "bstats-bukkit", "1.8")
}

bukkit {
    main = "me.weiwen.storagerobot.StorageRobot"
    name = "StorageRobot"
    version = project.version.toString()
    description = "Automatically sort inventory items into matching containers."
    apiVersion = "1.21.4"
    author = "Goh Wei Wen <goweiwen@gmail.com>"
    website = "weiwen.me"
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.set(listOf("-opt-in=kotlinx.serialization.ExperimentalSerializationApi"))

    sourceSets.main {
        java.srcDirs("src/main/kotlin")
    }
}

tasks.withType<ShadowJar> {
    fun reloc(pkg: String) = relocate(pkg, "$group.dependency.$pkg")

    reloc("org.bstats")
}

val pluginPath = project.findProperty("plugin_path")

if(pluginPath != null) {
    tasks {
        named<DefaultTask>("build") {
            dependsOn("shadowJar")
            doLast {
                copy {
                    from(findByName("reobfJar") ?: findByName("shadowJar") ?: findByName("jar"))
                    into(pluginPath)
                }
            }
        }
    }
}
