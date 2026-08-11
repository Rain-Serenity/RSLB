plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

version = "2.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    // 26.2 的 Mojang-mapped NMS 类（Paper/Folia）。
    // 来源：paperweight-userdev dev bundle 缓存（applyDevBundlePatches_.../output.jar），
    // 运行时由服务器 jar 提供。
    compileOnly(files("libs/nms-26.2.jar"))
    // 以下两个版本必须与 26.2 服务器自带库（vanilla bundler）保持一致，切勿升级！
    compileOnly("com.mojang:authlib:9.0.75")
    compileOnly("io.netty:netty-all:4.2.15.Final")

    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("com.mysql:mysql-connector-j:9.7.0")
    implementation("com.h2database:h2:2.4.240")
    implementation("org.spongepowered:configurate-yaml:4.2.0")
    implementation("org.spongepowered:configurate-core:4.2.0")
    implementation("org.yaml:snakeyaml:2.6")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("com.squareup.okio:okio-jvm:3.18.1")
    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-commons:9.10.1")
    implementation("com.google.code.gson:gson:2.14.0")
    // brigadier 与服务器自带版本（1.0.500 魔改版）二进制兼容，保持官方最新发布版
    implementation("com.mojang:brigadier:1.0.18")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    shadowJar {
        archiveBaseName = "RSLB"

        mergeServiceFiles()

        val relocate = listOf(
            "com.zaxxer.hikari" to "com.rserene.chosen.server.libs.hikari",
            "com.mysql" to "com.rserene.chosen.server.libs.mysql",
            "org.h2" to "com.rserene.chosen.server.libs.h2",
            "org.spongepowered.configurate" to "com.rserene.chosen.server.libs.configurate",
            "okhttp3" to "com.rserene.chosen.server.libs.okhttp",
            "okio" to "com.rserene.chosen.server.libs.okio",
            "org.objectweb.asm" to "com.rserene.chosen.server.libs.asm",
            "com.google.gson" to "com.rserene.chosen.server.libs.gson",
            "kotlin" to "com.rserene.chosen.server.libs.kotlin"
        )
        relocate.forEach { (from, to) -> relocate(from, to) }

        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")
        exclude("module-info.class")
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/kotlin-stdlib.kotlin_module")
        exclude("META-INF/okhttp3.kotlin_module")
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
