val testJavaVersion = project.findProperty("testJavaVersion")?.toString()?.toInt() ?: 17

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(testJavaVersion))
    }
}

listOf(17, 21).forEach { version ->
    tasks.register<Test>("testJava$version") {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(version))
        })
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
    }
}

val springBootVersion = "3.3.4"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(project(":spring-boot3-starter"))
    testImplementation(project(":spring-boot3-autoconfigure"))
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
