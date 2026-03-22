plugins {
    // ==================== Android 与 Kotlin 插件 ====================
    alias(libs.plugins.android.application)         // Android 应用插件
    alias(libs.plugins.kotlin.compose)              // Jetpack Compose 支持
    alias(libs.plugins.kotlin.serialization)        // Kotlin 序列化支持

    // ==================== 编译与依赖注入插件 ====================
    alias(libs.plugins.ksp)                         // Kotlin 符号处理器
    alias(libs.plugins.hilt)                        // Hilt 依赖注入

    alias(libs.plugins.secrets)                    // Android Secrets Gradle 插件
}

android {
    namespace = "top.atsuko.snowfoxweather"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "top.atsuko.snowfoxweather"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

secrets {
    propertiesFileName = "local.properties"
    ignoreList.add("sdk.*")
}

dependencies {

    // ==================== 核心与基础库 ====================
    implementation(libs.androidx.core.ktx)                      // AndroidX 核心扩展
    implementation(libs.androidx.lifecycle.runtime.ktx)         // 生命周期扩展
    implementation(libs.androidx.compose.ui)                    // Jetpack Compose UI
    implementation(libs.androidx.compose.ui.graphics)           // Compose UI 图形支持
    implementation(libs.androidx.compose.ui.geometry)           // Compose UI 几何支持

    // ==================== Compose 相关 ====================
    implementation(platform(libs.androidx.compose.bom)) // Compose 版本管理 BOM
    androidTestImplementation(platform(libs.androidx.compose.bom))      // Compose 测试 BOM
    implementation(libs.androidx.activity.compose)                      // Compose 与 Activity 集成
    implementation(libs.androidx.lifecycle.viewmodel.compose)           // Compose 与 ViewModel 集成
    implementation(libs.androidx.compose.ui.tooling.preview)            // Compose 预览支持
    debugImplementation(libs.androidx.compose.ui.tooling)               // Compose 工具支持
    implementation(libs.androidx.compose.material3)                     // Material Design 3
    implementation(libs.androidx.compose.material.icons.extended)       // Material 图标扩展

    // ==================== 依赖注入相关 ====================
    implementation(libs.hilt.android)                                   // Hilt 依赖注入
    ksp(libs.hilt.android.compiler)                                     // Hilt 编译器
    implementation(libs.androidx.hilt.navigation.compose)               // Hilt Compose 导航支持
    ksp(libs.androidx.hilt.compiler)                                    // Hilt 扩展编译器

    // ==================== 网络相关 ====================
    implementation(libs.retrofit)                                       // Retrofit 网络请求
    implementation(libs.logging.interceptor)                            // OkHttp 日志拦截器
    implementation(libs.kotlinx.serialization.json)                     // Kotlinx Serialization JSON
    implementation(libs.retrofit.converter.kotlinx.serialization)       // Retrofit Kotlinx Serialization 转换器
    implementation(libs.coil.compose)                                   // coil 图片加载组件
    implementation(libs.coil.network.okhttp)                            // coil OkHttp 支持
    implementation(libs.converter.scalars)                              // 字符串转换器
    implementation(libs.bcpkix.jdk18on)                                 // Bouncy Castle 加密库

    // ==================== 本地数据存储相关 ====================
    implementation(libs.androidx.datastore.preferences)                 // DataStore 偏好存储

    // ==================== 测试相关 ====================
    testImplementation(libs.junit)                                      // JUnit 单元测试
    androidTestImplementation(libs.androidx.junit)                      // AndroidX JUnit 测试
    androidTestImplementation(libs.androidx.espresso.core)              // Espresso UI 测试
    debugImplementation(libs.androidx.compose.ui.test.manifest)         // Compose UI 测试清单
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)     // Compose UI 测试规则
}