// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // ==================== Android 与 Kotlin 插件 ====================
    alias(libs.plugins.android.application) apply false     // Android 应用插件
    alias(libs.plugins.kotlin.compose) apply false          // Jetpack Compose 支持
    alias(libs.plugins.kotlin.serialization) apply false    // Kotlin 序列化支持

    // ==================== 编译与依赖注入插件 ====================
    alias(libs.plugins.hilt) apply false                    // Hilt 依赖注入
    alias(libs.plugins.ksp) apply false                     // Kotlin 符号处理器

    alias(libs.plugins.secrets) apply false                 // Android Secrets Gradle 插件
}