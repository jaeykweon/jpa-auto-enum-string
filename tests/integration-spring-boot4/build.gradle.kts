java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val springBootVersion = "4.0.6"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    // Use the hibernate6-adapter directly — the spring-boot-autoconfigure module targets
    // Spring Boot 2/3 and references HibernatePropertiesCustomizer from the old package.
    testImplementation(project(":core"))
    testImplementation(project(":hibernate6-adapter"))
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-hibernate")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
