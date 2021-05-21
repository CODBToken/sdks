import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.4.32"
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
}

group = "com.codb.sdk"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    maven(url = "https://maven.aliyun.com/nexus/content/groups/public/")
    mavenCentral()
}

noArg {
    annotation("com.codb.sdk.model.NoArgOpenDataClass")
}

allOpen {
    annotation("com.codb.sdk.model.NoArgOpenDataClass")
}

dependencies {
//    implementation("org.jetbrains.kotlin:kotlin-stdlib")
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.9")
    implementation("com.squareup.retrofit2:converter-gson:2.8.1")
    implementation("org.json:json:20210307")

    implementation("org.slf4j:slf4j-api:1.7.9")
    testImplementation("org.slf4j:slf4j-jdk14:1.7.9")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")


}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
