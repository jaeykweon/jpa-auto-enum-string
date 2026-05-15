plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    compileOnly("org.hibernate:hibernate-core:5.6.15.Final")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
