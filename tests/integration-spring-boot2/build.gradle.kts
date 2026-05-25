val springBootVersion: String = project.findProperty("springBootVersion")?.toString() ?: "2.7.18"
val testJavaVersion = project.findProperty("testJavaVersion")?.toString()?.toInt() ?: 8

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(testJavaVersion))
    }
}

listOf(8, 11, 17).forEach { version ->
    tasks.register<Test>("testJava$version") {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(version))
        })
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(project(":spring-boot2-starter"))
    testImplementation(project(":spring-boot2-autoconfigure"))
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
