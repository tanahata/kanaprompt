plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "jp.kanaprompt.ime"; compileSdk = 35
 defaultConfig { applicationId = "jp.kanaprompt.ime"; minSdk = 29; targetSdk = 35; versionCode = 22; versionName = "1.0.5" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
