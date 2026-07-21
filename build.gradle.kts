plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")

    maven {
        url = uri("https://repo.triumphteam.dev/snapshots/")
    }

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
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")

    implementation("dev.rollczi:litecommands-bukkit:3.10.9")
    implementation("dev.triumphteam:triumph-gui-paper:3.1.13-SNAPSHOT")

    compileOnly("dev.ua.ikeepcalm:circle-of-imagination-api:1.4.4-SNAPSHOT")
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

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
