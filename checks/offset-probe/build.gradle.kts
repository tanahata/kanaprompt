plugins { id("com.android.application") version "8.7.3" }

android {
    namespace = "jp.kanaflick.offsetprobe"
    compileSdk = 35
    defaultConfig {
        applicationId = "jp.kanaflick.offsetprobe"
        minSdk = 29
        targetSdk = 35
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
