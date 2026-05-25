val testJavaVersion = project.findProperty("testJavaVersion")?.toString()?.toInt() ?: 21

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(testJavaVersion))
    }
}

val springBootVersion = "4.0.6"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(project(":spring-boot4-starter"))
    testImplementation(project(":spring-boot4-autoconfigure"))
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
