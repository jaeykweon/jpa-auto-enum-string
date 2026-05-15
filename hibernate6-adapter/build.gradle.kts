plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    compileOnly("org.hibernate.orm:hibernate-core:6.4.4.Final")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
