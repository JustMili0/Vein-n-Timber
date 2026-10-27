plugins {
    // This plugin applies the correct loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
    alias(libs.plugins.publish)
}

// DO NOT set group = ...!
version = "${property("mod.version")}+mc${sc.current.version}-Fabric"
base.archivesName = property("mod.archives_base_name") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

// Used for publishing on Modrinth and Curseforge
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")?.asList().orEmpty().map { it.toString() }

// Versions before 26.1 are obfuscated (remapped), 26.1+ are not
val obfuscated = sc.current.parsed < "26.1"

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.terraformersmc.com/", "TerraformersMC", "com.terraformersmc") // Mod Menu
    strictMaven("https://maven.bawnorton.com/releases", "Bawnorton", "com.github.bawnorton") // MixinSquared

    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.enjarai.dev/mirrors") // MixinSquared mirror
    maven("https://maven.lumynitystudios.net/") // Lumynity Studios' mods
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Applies Mojang Mappings on obfuscated versions
    loomx.applyMojangMappings()

    // Use `mod{dependency type}` even on 26.1+ - loom-back-compat converts them
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    // Fabric dependencies go here
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    // Other
    modImplementation("com.terraformersmc:modmenu:${property("deps.mod_menu")}")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    accessWidenerPath = sc.process(
        rootProject.file("src/main/resources/vein_n_timber.accesswidener"),
        "build/processed.accesswidener"
    )

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Shares the run directory between versions
        jvmArguments.add("-Dmixin.debug.export=true") // Exports transformed classes for debugging
    }
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("mod_id", sc.properties["mod.id"])
            register("mod_name", sc.properties["mod.name"])
            register("mod_version", sc.properties["mod.version"])
            register("mod_description", sc.properties["mod.description"])
            register("mod_authors", sc.properties["mod.authors"])
            register("mod_license", sc.properties["mod.license"])
            register("fabric_loader_version", sc.properties["deps.fabric_loader"])
            register("fabric_api_version", sc.properties["deps.fabric_api"])
            register("minecraft_version_constraint", sc.properties["mod.mc_compat"])
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Includes the license file in the built mod
    withType<Jar> {
        from(rootProject.file("LICENSE")) { rename { it } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        // loomx.modJar returns the jar task for the applied loom variant
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

publishMods {
    file = loomx.modJar.flatMap { it.archiveFile }
    modLoaders.add("fabric")

    // TODO: bring back once build-logic is ported from multiloader to fabric-only
    //changelog = readChangelogFromBranch("origin/rep-info", ".Changelogs/${property("mod.version")}-Changelog.md")
    changelog = rootProject.file(".Changelogs/${property("mod.version")}-Changelog.md").readText(Charsets.UTF_8)

    modrinth {
        accessToken = providers.gradleProperty("modrinth_token")
        projectId = "sS6j8wrN"

        minecraftVersions.addAll(compatibleVersions)
        environment = SERVER_ONLY_CLIENT_OPTIONAL
        // STABLE, BETA, ALPHA
        type = STABLE

        requires("fabric-api")
    }
}