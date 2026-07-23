# R8 is used for shrinking + optimization only. Obfuscation stays OFF so the stack
# traces shown by CrashActivity (the only crash reporting this app has) remain readable
# without needing a mapping file per release.
-dontobfuscate

# Keep source/line info so crash traces point at real lines.
-keepattributes SourceFile,LineNumberTable

# WorkManager instantiates workers reflectively by class name persisted in its own DB.
# work-runtime ships an equivalent consumer rule; kept here explicitly so a library
# update can never silently drop it — background sync dying on a POS is not acceptable.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# OkHttp's optional conscrypt/bouncycastle/openjsse integrations are compile-time only.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
