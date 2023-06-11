import de.undercouch.gradle.tasks.download.Download
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("de.undercouch.download").version("5.3.0")
    id("com.github.johnrengelman.shadow") version "7.1.2"

}

group = "dev.meyi.bazaarnotifier"
version = "1.6.2-beta3"
val mod_id = "bazaarnotifier"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    launchConfigs.named("client") {
        arg("--tweakClass", "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker")
    }
    log4jConfigs.from(file("log4j2.xml"))
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

sourceSets.main {
    output.setResourcesDir(file("$buildDir/classes/java/main"))
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://repo.polyfrost.cc/releases")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
    modCompileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.0-alpha+")
    shade("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+")
}



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

tasks{
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
    withType(Jar::class) {
        manifest.attributes.run {
            this["FMLCorePluginContainsFMLMod"] = "true"
            this["ForceLoadAsMod"] = "true"
        }
        named<ShadowJar>("shadowJar") {
            archiveClassifier.set("dev")
            configurations = listOf(shade)
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        remapJar {
            input.set(shadowJar.get().archiveFile)
            archiveClassifier.set("")
        }

       jar {
            manifest {
                attributes(
                    mapOf(
                        "ModSide" to "CLIENT",
                        "ForceLoadAsMod" to true,
                        "TweakOrder" to "0",
                        "MixinConfigs" to "mixin.${mod_id}.json",
                        "TweakClass" to "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker"
                    )
                )
            }
            dependsOn(shadowJar)
            archiveClassifier.set("")
            enabled = false
        }
        processResources {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE

        }
    }
}
