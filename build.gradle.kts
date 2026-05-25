import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
}

allprojects {
    group = property("group").toString()
    version = property("version").toString()

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    // Test modules use toolchains and manage their own Java version
    if (!path.startsWith(":tests:")) {
        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
    }

    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = true))
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()
            pom {
                url.set("https://github.com/jaeykweon/jpa-auto-enum-string")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("jaeykweon")
                        name.set("Gwon Jaeyong")
                        email.set("jaeykweon@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/jaeykweon/jpa-auto-enum-string")
                    connection.set("scm:git:git://github.com/jaeykweon/jpa-auto-enum-string.git")
                    developerConnection.set("scm:git:ssh://git@github.com/jaeykweon/jpa-auto-enum-string.git")
                }
            }
        }
    }
}

// Runs the full Java version matrix equivalent to CI
tasks.register("testAllJavaVersions") {
    dependsOn(
        ":tests:integration-spring-boot2:testJava8",
        ":tests:integration-spring-boot2:testJava11",
        ":tests:integration-spring-boot2:testJava17",
        ":tests:integration-spring-boot3:testJava17",
        ":tests:integration-spring-boot3:testJava21",
        ":tests:integration-spring-boot4:testJava21",
    )
}
