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
        artifactId = "jpa-auto-enum-string-spring-boot3-autoconfigure",
        version = project.version.toString()
    )
    pom {
        name.set("JPA Auto Enum String - Spring Boot 3 Auto-configuration")
        description.set("Spring Boot 3 auto-configuration for jpa-auto-enum-string")
    }
}

dependencies {
    implementation(project(":core"))
    compileOnly(project(":hibernate6-adapter"))

    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:3.3.4"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.hibernate.orm:hibernate-core")
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:3.3.4"))
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
