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
        artifactId = "jpa-auto-enum-string-spring-boot4-autoconfigure",
        version = project.version.toString()
    )
    pom {
        name.set("JPA Auto Enum String - Spring Boot 4 Auto-configuration")
        description.set("Spring Boot 4 auto-configuration for jpa-auto-enum-string")
    }
}

dependencies {
    implementation(project(":core"))
    compileOnly(project(":hibernate6-adapter"))

    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:4.0.6"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot-hibernate")
    compileOnly("org.hibernate.orm:hibernate-core")
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:4.0.6"))
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
