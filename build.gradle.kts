plugins {
    java
    id("net.fabricmc.fabric-loom") version "1.17.17"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

base {
    archivesName = providers.gradleProperty("archives_base_name").get()
}

repositories {
    mavenCentral()
    maven("https://maven.caffeinemc.net/releases")
}

loom {
    splitEnvironmentSourceSets()
    accessWidenerPath = file("src/main/resources/mipmapplus.accesswidener")

    mods {
        register("mipmapplus") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }
}

sourceSets {
    named("main") {
        java.setSrcDirs(emptyList<String>())
    }
    named("client") {
        java.srcDir("src/main/java")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    compileOnly("net.caffeinemc:sodium-fabric:0.9.1+mc26.2")
}

tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "minecraft_version" to providers.gradleProperty("minecraft_version").get(),
        "loader_version" to providers.gradleProperty("loader_version").get()
    )
    inputs.properties(properties)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}

tasks.withType<Jar>().configureEach {
    from("LICENSE")
    from("THIRD_PARTY_NOTICES.md")
}
