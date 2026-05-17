plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(project(":core"))
    compileOnly(project(":hibernate6-adapter"))

    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:4.0.6"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot-hibernate")
    compileOnly("org.hibernate.orm:hibernate-core")
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:4.0.6"))
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
}
