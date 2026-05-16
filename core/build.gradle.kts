plugins {
    `java-library`
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
