val springBootVersion: String = project.findProperty("springBootVersion")?.toString() ?: "2.7.18"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(project(":spring-boot2-starter"))
    testImplementation(project(":spring-boot2-autoconfigure"))
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
