# Crash reports: keep line numbers in mapping.txt
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class fumei.faruk.dev.br.data.** { *; }

# ViewModels (reflection-free, but safe for R8)
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# Compose runtime stubs used by generated code
-dontwarn androidx.compose.**
