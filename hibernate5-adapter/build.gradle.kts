plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "jpa-auto-enum-string-hibernate5-adapter",
        version = project.version.toString()
    )
    pom {
        name.set("JPA Auto Enum String - Hibernate 5 Adapter")
        description.set("Hibernate 5 integrator for jpa-auto-enum-string")
    }
}

dependencies {
    api(project(":core"))
    compileOnly("org.hibernate:hibernate-core:5.6.15.Final")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
