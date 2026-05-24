plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "jpa-auto-enum-string-spring-boot2-starter",
        version = project.version.toString()
    )
    pom {
        name.set("JPA Auto Enum String - Spring Boot 2 Starter")
        description.set("Spring Boot 2 starter for jpa-auto-enum-string. Eliminates @Enumerated(EnumType.STRING) boilerplate from every JPA enum field.")
    }
}

dependencies {
    api(project(":core"))
    implementation(project(":spring-boot2-autoconfigure"))
    runtimeOnly(project(":hibernate5-adapter"))
}
