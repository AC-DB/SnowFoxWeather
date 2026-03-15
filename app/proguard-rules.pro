# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ========== 基础指令  ==========
# 代码混淆压缩比，在0~7之间
-optimizationpasses 5
# 混合时不使用大小写混合，混淆后的类名为小写
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库类
-dontskipnonpubliclibraryclasses
# 不做预校验，可加快混淆速度
-dontpreverify
# 输出混淆日志
-verbose

# ========== 保留 Android 组件和基本结构 ==========
# 保留四大组件、Application、Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Fragment
#-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment

# 保留 R 文件中的所有资源 ID 不被混淆，避免资源引用失效
-keep class **.R$* { *; }

# 保留自定义 View 不被混淆，避免在布局文件中引用时出错
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# ========== 处理特定的模式 ==========
# 保留 Parcelable 的实现，确保其 CREATOR 字段不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留枚举类中的 values 方法，避免序列化问题
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留通过注解 @Keep 标记的类成员不被混淆
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

# 保留 Native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留 JavaScript 接口（用于 WebView）
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ========== 保留第三方库 ==========
# 保留 Gson 的泛型信息，避免数据解析失败
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Retrofit 2 和 OkHttp3 的混淆规则
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions
-keepattributes Signature

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ========== 保留用于反射的类和成员 ==========
# 如果某些类或方法通过反射调用，需要手动保留
# -keep class com.yourpackage.YourReflectedClass { *; }

# ========== 调试与日志 ==========
# 保留行号信息和源文件属性，便于混淆后能还原堆栈跟踪
-keepattributes SourceFile, LineNumberTable
# 保留注解
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# ========== 忽略警告 ==========
# -ignorewarnings