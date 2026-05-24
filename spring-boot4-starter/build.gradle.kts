plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "jpa-auto-enum-string-spring-boot4-starter",
        version = project.version.toString()
    )
    pom {
        name.set("JPA Auto Enum String - Spring Boot 4 Starter")
        description.set("Spring Boot 4 starter for jpa-auto-enum-string. Eliminates @Enumerated(EnumType.STRING) boilerplate from every JPA enum field.")
    }
}

dependencies {
    api(project(":core"))
    implementation(project(":spring-boot4-autoconfigure"))
    runtimeOnly(project(":hibernate6-adapter"))
}
