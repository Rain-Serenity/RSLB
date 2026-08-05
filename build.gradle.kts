plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    // Mojang-mapped NMS classes for 26.2 (Luminol/Folia). Source: paperweight-userdev
    // dev bundle cache (applyDevBundlePatches_.../output.jar). At runtime these classes
    // are provided by the server jar.
    compileOnly(files("libs/nms-26.2.jar"))
    // Versions match the libraries shipped in the 26.2 server (vanilla bundler)
    compileOnly("com.mojang:authlib:9.0.75")
    compileOnly("io.netty:netty-all:4.2.15.Final")

    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("mysql:mysql-connector-java:8.0.29")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.spongepowered:configurate-core:4.1.2")
    implementation("org.yaml:snakeyaml:1.33")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okio:okio:3.3.0")
    implementation("org.ow2.asm:asm:9.7.1")
    implementation("org.ow2.asm:asm-commons:9.7.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.mojang:brigadier:1.0.18")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    shadowJar {
        archiveBaseName = "RSLB"
        archiveVersion = "1.0-SNAPSHOT"

        mergeServiceFiles()

        val relocate = listOf(
            "com.zaxxer.hikari" to "com.rserene.chosen.server.libs.hikari",
            "com.mysql" to "com.rserene.chosen.server.libs.mysql",
            "org.h2" to "com.rserene.chosen.server.libs.h2",
            "org.spongepowered.configurate" to "com.rserene.chosen.server.libs.configurate",
            "okhttp3" to "com.rserene.chosen.server.libs.okhttp",
            "okio" to "com.rserene.chosen.server.libs.okio",
            "org.objectweb.asm" to "com.rserene.chosen.server.libs.asm"
        )
        relocate.forEach { (from, to) -> relocate(from, to) }

        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
