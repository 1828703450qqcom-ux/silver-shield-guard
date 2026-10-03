# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# 保留 Room 实体类
-keep class com.yindun.shouhu.data.local.entity.** { *; }

# 保留 ONNX Runtime
-keep class ai.onnxruntime.** { *; }

# 保留 Kotlin 协程
-keep class kotlinx.coroutines.** { *; }

# 保留 Compose
-dontwarn androidx.compose.**

# 保留 Room DAO
-keep class com.yindun.shouhu.data.local.dao.** { *; }

# 保留 Repository
-keep class com.yindun.shouhu.data.repository.** { *; }

# 保留 Service
-keep class com.yindun.shouhu.service.** { *; }

# 保留数据类
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 Parcelable
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# 保留 Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 优化
-optimizations !code/simplification/advanced,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
