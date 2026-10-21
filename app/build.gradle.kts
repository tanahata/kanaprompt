plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "jp.kanaflick.ime"; compileSdk = 35
 defaultConfig { applicationId = "jp.kanaflick.ime"; minSdk = 29; targetSdk = 35; versionCode = 17; versionName = "1.0.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
