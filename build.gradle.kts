import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import java.util.Locale

plugins {
    java
    application
    checkstyle
    jacoco
    alias(libs.plugins.spotless)
    alias(libs.plugins.spotbugs)
}

group = "academy"
version = "1.0.0"

val javaVersion = JavaVersion.VERSION_21
if (JavaVersion.current() < javaVersion) {
    throw GradleException(
        """
        |
        |Нужен JDK $javaVersion или новее, а сборка запущена на JDK ${JavaVersion.current()}.
        |
        |IntelliJ IDEA: Settings -> Build Tools -> Gradle -> Gradle JVM.
        |Терминал: установи JDK 21 и укажи его в JAVA_HOME.
        |
        |Сейчас используется: ${System.getProperty("java.home")}
        """.trimMargin()
    )
}

val utf8JvmArgs = listOf("-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8")

application {
    mainClass = "academy.cashbackmerge.Main"
    applicationName = "cashback-merge"
    applicationDefaultJvmArgs = utf8JvmArgs
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

dependencies {
    implementation(libs.picocli)

    implementation(platform(libs.jackson.bom))
    implementation(libs.bundles.jackson)

    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    compileOnly(libs.spotbugs.annotations)
    testCompileOnly(libs.spotbugs.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Раскомментируй, если понадобится:
    // testImplementation(platform(libs.mockito.bom))     // моки
    // testImplementation(libs.bundles.mockito)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaVersion.majorVersion.toInt()
    options.compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(utf8JvmArgs)

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) = Unit
        override fun beforeTest(test: TestDescriptor) = Unit
        override fun afterTest(test: TestDescriptor, result: TestResult) = Unit
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent != null) return
            logger.lifecycle(
                "\nТесты: ${result.testCount} всего, ${result.successfulTestCount} прошло, " +
                    "${result.failedTestCount} упало, ${result.skippedTestCount} пропущено."
            )
            if (result.skippedTestCount > 0) {
                logger.lifecycle("Пропущенные — обязательные тесты с @Disabled. См. README.\n")
            }
        }
    })

    // Читает CliRunner, чтобы запустить программу отдельным процессом.
    val runtimeClasspath = sourceSets.main.get().runtimeClasspath
    inputs.files(runtimeClasspath).withNormalizer(ClasspathNormalizer::class)
    doFirst {
        systemProperty("academy.cli.classpath", runtimeClasspath.asPath)
        systemProperty("academy.cli.mainClass", application.mainClass.get())
    }
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat(libs.versions.palantir.java.format.get())
            .style("PALANTIR")
            .formatJavadoc(true)
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        toggleOffOn()
    }
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configFile = file("config/checkstyle/checkstyle.xml")
    configDirectory = layout.projectDirectory.dir("config/checkstyle")
    isIgnoreFailures = false
    maxWarnings = 0
}

spotbugs {
    toolVersion = libs.versions.spotbugs.get()
    effort = Effort.MAX
    reportLevel = Confidence.DEFAULT
    excludeFilter = file("config/spotbugs/exclude.xml")
}

tasks.spotbugsMain {
    reports.create("html") {
        required = true
        setStylesheet("fancy-hist.xsl")
    }
    reports.create("xml") { required = true }
}

// На тестовом коде SpotBugs слишком шумит, пользы мало.
tasks.spotbugsTest {
    enabled = false
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
        xml.required = true
        csv.required = true
    }
}

// Строку с процентом покрытия вылавливает регулярка в .gitlab-ci.yml.
tasks.register("printCoverage") {
    dependsOn(tasks.jacocoTestReport)
    val csvReport = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.csv")
    doLast {
        val file = csvReport.get().asFile
        if (!file.exists()) {
            logger.lifecycle("Отчёт о покрытии не найден — тесты не запускались.")
            return@doLast
        }
        // Колонки: GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,...
        var missed = 0L
        var covered = 0L
        file.readLines().drop(1).filter { it.isNotBlank() }.forEach {
            val columns = it.split(",")
            missed += columns[3].toLong()
            covered += columns[4].toLong()
        }
        val total = missed + covered
        val percent = if (total == 0L) 0.0 else covered * 100.0 / total
        // Locale.ROOT: иначе «0,0%» с запятой, и регулярка в CI не совпадёт.
        logger.lifecycle(String.format(Locale.ROOT, "Покрытие инструкций: %.1f%%", percent))
    }
}

// Включается флагом: ./gradlew check -PcoverageGate=true
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.50".toBigDecimal()
            }
        }
    }
}

if (providers.gradleProperty("coverageGate").orNull == "true") {
    tasks.check {
        dependsOn(tasks.jacocoTestCoverageVerification)
    }
}
