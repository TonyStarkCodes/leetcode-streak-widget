# Add project specific ProGuard rules here.
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.tonystarkcodes.leetcodestreakwidget.data.model.** { *; }
