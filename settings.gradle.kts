@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}

// F-Droid doesn't support foojay-resolver plugin
// plugins {
//     id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
// }

rootProject.name = "Metrolist"
include(":app")
include(":innertube")
include(":kugou")
include(":lrclib")
include(":kizzy")

// Use a local copy of NewPipe Extractor by uncommenting the lines below.
// We assume, that Metrolist and NewPipe Extractor have the same parent directory.
// If this is not the case, please change the path in includeBuild().
//
// For this to work you also need to change the implementation in innertube/build.gradle.kts
// to one which does not specify a version.
// From:
//      implementation(libs.newpipe.extractor)
// To:
//      implementation("com.github.teamnewpipe:NewPipeExtractor")
//includeBuild("../NewPipeExtractor") {
//    dependencySubstitution {
//        substitute(module("com.github.teamnewpipe:NewPipeExtractor")).using(project(":extractor"))
//    }
//}

// Require local PipePipeExtractor (latest) via composite build
val localPipePipe = file("external/PipePipeExtractor")
val localPipePipeHasSettings =
    localPipePipe.resolve("settings.gradle").exists() || localPipePipe.resolve("settings.gradle.kts").exists()
check(localPipePipe.exists() && localPipePipeHasSettings) {
    "PipePipeExtractor not found. Initialize submodule or clone to external/PipePipeExtractor before building."
}
includeBuild("external/PipePipeExtractor") {
    dependencySubstitution {
        substitute(module("com.github.InfinityLoop1308:PipePipeExtractor")).using(project(":"))
        substitute(module("com.github.InfinityLoop1308.PipePipeExtractor:extractor")).using(project(":extractor"))
        substitute(module("com.github.InfinityLoop1308.PipePipeExtractor:timeago-parser")).using(project(":timeago-parser"))
    }
}
