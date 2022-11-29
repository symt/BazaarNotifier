import de.undercouch.gradle.tasks.download.Download

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("de.undercouch.download").version("5.3.0")
}

group = "dev.meyi.bazaarnotifier"
version = "1.5.0-beta15"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

sourceSets.main {
    output.setResourcesDir(file("$buildDir/classes/java/main"))
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
}

// Tasks:

val resourcesFile = "src/main/resources/resources.json"
val resourcesURL = "https://raw.githubusercontent.com/symt/BazaarNotifier/resources/resources.json"

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.property("version", project.version)
    inputs.property("mcversion", "1.8.9")
    from(sourceSets["main"].resources.srcDirs) {
        include("mcmod.info")
        expand("version" to project.version, "mcversion" to "1.8.9")
    }
    from(sourceSets["main"].resources.srcDirs) {
        exclude("mcmod.info")
    }

    dependsOn("retrieveResources")
    finalizedBy("destroyResources")
    outputs.upToDateWhen { false }
}

task<DefaultTask>("destroyResources") {
    doLast {
        if (File(resourcesFile).exists()) {
            project.delete(files(resourcesFile))
        }
    }
    outputs.upToDateWhen { false }
}

task<DefaultTask>("retrieveResources") {
    val dest = File(resourcesFile)

    if (dest.exists()) {
        project.delete(files(resourcesFile))
    }
    task<Download>("download-task") {
        src(resourcesURL)
        dest(resourcesFile)
    }
    dependsOn("download-task")
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
    }
}