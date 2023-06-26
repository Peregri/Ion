plugins {
	kotlin("plugin.serialization")
	kotlin("jvm")

	`java-library`
}

repositories {
	mavenCentral()

	maven("https://libraries.minecraft.net")
}

dependencies {
	compileOnly("net.kyori:adventure-api:4.14.0")
	compileOnly("net.kyori:adventure-text-logger-slf4j:4.14.0")
	compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
	compileOnly("com.mojang:brigadier:1.0.18")

	compileOnlyApi("com.mysql:mysql-connector-j:8.0.33")

	api(kotlin("reflect"))
	api(kotlin("stdlib"))

	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

	api("org.jetbrains.exposed:exposed-core:0.41.1")
	api("org.jetbrains.exposed:exposed-dao:0.41.1")
	api("org.jetbrains.exposed:exposed-jdbc:0.41.1")
	api("org.jetbrains.exposed:exposed-java-time:0.41.1")

	// Temporary dependency needed for ObjectId Support
	api("org.litote.kmongo:kmongo-shared:4.9.0")
	api("org.mongodb:bson:4.10.1")

	api("com.zaxxer:HikariCP:5.0.1")

	api("com.h2database:h2:2.1.214")

	api("redis.clients:jedis:4.4.3")
}

kotlin.jvmToolchain(17)
