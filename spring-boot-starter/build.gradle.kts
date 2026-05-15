plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

dependencies {
    api(project(":core"))
    implementation(project(":spring-boot-autoconfigure"))
    runtimeOnly(project(":hibernate5-adapter"))
    runtimeOnly(project(":hibernate6-adapter"))
}
