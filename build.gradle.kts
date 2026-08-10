import java.util.Base64

plugins {
    java
    id("dev.firstdark.unimined") version "1.0.5+1.4.2-SNAPSHOT"
}

group = "wily.legacy125"
version = providers.gradleProperty("mod_version").get()

base {
    archivesName.set("Legacy4J-1.2.5")
}

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.wagyourtail.xyz/releases")
    maven("https://maven.wagyourtail.xyz/snapshots")
}

java {
    withSourcesJar()
}

unimined.minecraft {
    side("client")
    version(providers.gradleProperty("minecraft_version").get())

    mappings {
        mcp("legacy", providers.gradleProperty("mcp_version").get())
    }

    jarMod {}
}

dependencies {
    "jarMod"("risugami:modloader:${providers.gradleProperty("minecraft_version").get()}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.encoding = "UTF-8"
}

// Unimined 1.x attaches the transformed MCP game classpath directly to compileJava.
// Reuse that exact classpath so logical integration tests execute real 1.2.5 code.
val compileGameJava = tasks.named<JavaCompile>("compileJava")
tasks.named<JavaCompile>("compileTestJava") {
    classpath += compileGameJava.get().classpath
}

val decodedLegacyTextures = layout.buildDirectory.dir("generated/legacyTextures")
val decodeLegacyTextures by tasks.registering {
    val encoded = layout.projectDirectory.dir("src/main/encodedResources")
    inputs.dir(encoded)
    outputs.dir(decodedLegacyTextures)
    doLast {
        val sourceRoot = encoded.asFile.toPath()
        fileTree(encoded).matching { include("**/*.b64") }.forEach { source ->
            val relative = sourceRoot.relativize(source.toPath()).toString().removeSuffix(".b64")
            val target = decodedLegacyTextures.get().file(relative).asFile
            target.parentFile.mkdirs()
            target.writeBytes(Base64.getMimeDecoder().decode(source.readText()))
        }
    }
}

tasks.processResources {
    dependsOn(decodeLegacyTextures)
    from(decodedLegacyTextures)
}

tasks.test {
    useJUnitPlatform()
    classpath += compileGameJava.get().classpath
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "Legacy4J 1.2.5 Backport",
            "Implementation-Version" to project.version,
            "Built-For" to "Minecraft 1.2.5 + ModLoader"
        )
    }
}
