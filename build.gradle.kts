plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")

    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://repo.panda-lang.org/releases")
    }

    maven {
        url = uri("https://nexus.mysterria.net/repository/plugin-public/")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/creatorfromhell/")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven("https://repo.xenondevs.xyz/releases") {
        content { includeGroupAndSubgroups("xyz.xenondevs") }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")

    implementation("dev.rollczi:litecommands-bukkit:3.10.9")
    implementation("xyz.xenondevs.invui:invui:2.5.1")

    compileOnly("dev.ua.ikeepcalm:circle-of-imagination-api:1.5.2-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.google.code.gson:gson:2.13.2")
    compileOnly("io.github.alexdev03:unlimitednametags-api:2.0.0")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        relocate("dev.rollczi.litecommands", "net.mysterria.titles.libs.litecommands")
        relocate("xyz.xenondevs.invui", "net.mysterria.titles.libs.invui")
        mergeServiceFiles()
    }

    val syncNexo by registering(Copy::class) {
        val resources = file(providers.gradleProperty("mysterriaResources").orElse("../mysterria-resources").get())
        onlyIf("mysterria-resources checkout found") { resources.isDirectory }
        from(resources.resolve("glyphs/titles.yml")) { into("glyphs") }
        from(resources.resolve("pack/assets/titles")) { into("pack/assets/titles") }
        into(layout.projectDirectory.dir("run/plugins/Nexo"))
    }

    runServer {
        dependsOn(syncNexo)
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
