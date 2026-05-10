plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(project(":spring-boot-autoconfigure"))
    runtimeOnly(project(":hibernate5-adapter"))
    runtimeOnly(project(":hibernate6-adapter"))
}
