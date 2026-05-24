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

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<Test> {
        useJUnitPlatform()
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
