plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "jpa-auto-enum-string-core",
        version = project.version.toString()
    )
    pom {
        name.set("JPA Auto Enum String - Core")
        description.set("Core scanning logic for jpa-auto-enum-string")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
