plugins {
    id("java")
    id("io.quarkus") version "3.39.4"
}

group = "com.kevlaw"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Quarkus bom that includes quarkus rest
    implementation(platform("io.quarkus:quarkus-bom:3.39.4"))
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-mutiny") // reactive async

    // Hibernate ORM specific dependencies
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-reactive") // reactive async hibernate ORM
    implementation("io.quarkus:quarkus-flyway") // versioned database migrations

    // JDBC driver dependencies - Quarkus Dev Services should pick this up and spin up a local PG instance
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-reactive-pg-client") // reactive async DB driver

    // Platform and Logging
    implementation("org.jboss.slf4j:slf4j-jboss-logmanager")
    implementation("org.slf4j:slf4j-api:2.0.18")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    // Reactive application libs


}

tasks.test {
    useJUnitPlatform()
}
