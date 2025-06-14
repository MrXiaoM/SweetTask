plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "8.3.0"
}

group = "top.mrxiaom.sweet.taskplugin"
version = "1.0.0"
val targetJavaVersion = 8
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
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    // compileOnly("org.spigotmc:spigot:1.20") // NMS

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("org.black_ixx:playerpoints:3.2.7")

    implementation("net.kyori:adventure-api:4.21.0")
    implementation("net.kyori:adventure-platform-bukkit:4.4.0")
    implementation("net.kyori:adventure-text-minimessage:4.21.0")
    implementation("de.tr7zw:item-nbt-api:2.15.0")
    implementation("com.zaxxer:HikariCP:4.0.3") { isTransitive = false }
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("top.mrxiaom:PluginBase:1.4.5")

    testImplementation("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    testImplementation("junit:junit:4.13.2")
}
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks {
    jar {
        archiveClassifier.set("api")
    }
    shadowJar {
        archiveClassifier.set("")
        mapOf(
            "org.intellij.lang.annotations" to "annotations.intellij",
            "org.jetbrains.annotations" to "annotations.jetbrains",
            "top.mrxiaom.pluginbase" to "base",
            "com.zaxxer.hikari" to "hikari",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "net.kyori" to "kyori",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
    create("release")
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf(
                "version" to if (isRelease) {
                    project.version
                } else {
                    "${project.version}-SNAPSHOT"
                }
            ))
            include("plugin.yml")
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
        }
    }
}
