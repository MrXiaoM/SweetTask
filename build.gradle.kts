import top.mrxiaom.gradle.LibraryHelper

plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "9.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.8.0")
}
val base = LibraryHelper(project)

println("Group:   $group")
println("Version: $version")

val targetJavaVersion = 8
val pluginBaseModules = base.modules.run { listOf(library, message, paper, l10n, actions, gui, misc) }
val shadowGroup = "top.mrxiaom.sweet.taskplugin.libs"
var isRelease = gradle.startParameter.taskNames.run {
    contains("release") || contains("publishToMavenLocal")
}

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://mvn.lumine.io/repository/maven/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://r.irepo.space/maven/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    // compileOnly("org.spigotmc:spigot:1.20") // NMS

    // Vault
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    // PlayerPoints
    compileOnly("org.black_ixx:playerpoints:3.3.4")
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.12.2")
    // MythicMobs
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")
    // MMOItems
    compileOnly("io.lumine:MythicLib-dist:1.7.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT")
    // ItemsAdder
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    // CustomFishing
    compileOnly("net.momirealms:custom-fishing:2.3.22")
    // CraftEngine
    compileOnly("net.momirealms:craft-engine-core:26.7.4")
    compileOnly("net.momirealms:craft-engine-bukkit:26.7.4")
    // SX-Item
    compileOnly("com.github.Saukiya:SX-Item:4.4.9")
    // NeigeItems
    compileOnly("pers.neige.neigeitems:NeigeItems:1.21.128")
    compileOnly(base.depend.annotations)

    base.library(LibraryHelper.adventure("4.25.0"))
    base.library(base.depend.HikariCP)
    base.collectPluginHolders()

    implementation("de.tr7zw:item-nbt-api:2.16.0")
    for (artifact in pluginBaseModules) {
        implementation(artifact)
    }
    implementation(base.resolver.lite)

    testImplementation("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    testImplementation("junit:junit:4.13.2")
}
buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.taskplugin")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
    buildConfigField("boolean", "IS_DEVELOPMENT_BUILD", isRelease.not().toString())
}
configurations.compileOnly.configure {
    exclude(group="org.jspecify", module="jspecify")
}

val pluginVersion = if (isRelease) {
    project.version.toString()
} else {
    "${project.version}-SNAPSHOT"
}

LibraryHelper.initJava(project, base, targetJavaVersion, true, pluginVersion)
LibraryHelper.initPublishing(project)

tasks {
    register("release")
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
}
