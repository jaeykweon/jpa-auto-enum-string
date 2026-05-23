java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":hibernate6-adapter"))
    implementation("org.hibernate.orm:hibernate-core:6.4.4.Final")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.h2database:h2:2.2.224")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
