plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":core"))
    compileOnly(project(":hibernate6-adapter"))

    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:3.3.4"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.hibernate.orm:hibernate-core")
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:3.3.4"))
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
}
