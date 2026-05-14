plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    compileOnly(project(":hibernate5-adapter"))
    compileOnly(project(":hibernate6-adapter"))

    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.hibernate:hibernate-core")
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
