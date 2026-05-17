plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

dependencies {
    api(project(":core"))
    implementation(project(":spring-boot2-autoconfigure"))
    runtimeOnly(project(":hibernate5-adapter"))
}
