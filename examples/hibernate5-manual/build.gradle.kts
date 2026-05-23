dependencies {
    implementation(project(":hibernate5-adapter"))
    implementation("org.hibernate:hibernate-core:5.6.15.Final")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.h2database:h2:2.2.224")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
