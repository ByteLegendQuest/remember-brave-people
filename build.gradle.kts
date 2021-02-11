plugins {
    application
    checkstyle
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.1")
    implementation("com.aliyun.oss:aliyun-sdk-oss:3.8.0")
    implementation("commons-io:commons-io:2.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.mockito:mockito-junit-jupiter:3.7.7")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

application {
    // Define the main class for the application.
    mainClass.set("com.bytelegend.game.CIDataGeneratorJob")
}

tasks.register("dockerBuild") {
    dependsOn("installDist")

    doLast {
        project.exec {
            commandLine("docker", "build", ".", "-t", "blindpirate/remember-brave-people")
        }
    }
}

tasks.test {
    if (System.getProperty("includeDockerTest") == "true") {
        dependsOn("dockerBuild")
        systemProperty("includeDockerTest", "true")
    }
    useJUnitPlatform()
}

tasks.register<JavaExec>("dailyRun") {
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs("-DworkspaceDir=${rootProject.rootDir.absolutePath}")
    if (System.getProperty("ossAccessKeyId") != null) {
        jvmArgs("-DossAccessKeyId=${System.getProperty("ossAccessKeyId")}")
    }
    if (System.getProperty("ossAccessKeySecret") != null) {
        jvmArgs("-DossAccessKeySecret=${System.getProperty("ossAccessKeySecret")}")
    }
    mainClass.set("com.bytelegend.game.CIDailyDataGeneratorJob")
}

tasks.register<JavaExec>("localRun") {
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs("-DworkspaceDir=${rootProject.rootDir.absolutePath}")
    mainClass.set("com.bytelegend.game.DeveloperLocalDataGeneratorJob")
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}
