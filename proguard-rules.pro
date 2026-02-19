# LibreOffice SDK ProGuard Rules

# Keep JNI classes
-keep class org.libreoffice.kit.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep SDK public API
-keep class org.libreoffice.sdk.** { *; }
