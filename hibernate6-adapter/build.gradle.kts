plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "jpa-auto-enum-string-hibernate6-adapter",
        version = project.version.toString()
    )
    pom {
        name.set("JPA Auto Enum String - Hibernate 6/7 Adapter")
        description.set("Hibernate 6 and 7 integrator for jpa-auto-enum-string")
    }
}

dependencies {
    api(project(":core"))
    compileOnly("org.hibernate.orm:hibernate-core:6.4.4.Final")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
