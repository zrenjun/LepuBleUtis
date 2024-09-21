// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}


subprojects {
    project.plugins.whenPluginAdded {
        if (this is org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin) {
            project.extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
                sourceSets.forEach { sourceSet ->
                    sourceSet.kotlin.srcDir("build/generated/ksp/${sourceSet.name}/kotlin")
                }
            }
        }
    }
}