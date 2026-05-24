plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "jpa-auto-enum-string-spring-boot2-autoconfigure",
        version = project.version.toString()
    )
    pom {
        name.set("JPA Auto Enum String - Spring Boot 2 Auto-configuration")
        description.set("Spring Boot 2 auto-configuration for jpa-auto-enum-string")
    }
}

dependencies {
    implementation(project(":core"))
    compileOnly(project(":hibernate5-adapter"))

    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.hibernate:hibernate-core")
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
