object Versions {
    val legacySupportV13 = "1.0.0"
    val playServicesAds = "19.0.1"
    val constraintLayout = "1.1.3"
    val material = "1.0.0"
    val gson = "2.8.6"
    val glide = "4.10.0"
    val appcompat = "1.1.0"
    val consentLibrary = "1.0.8"
    val coreKTX = "1.2.0"
    val lifecycle = "2.2.0"
    val lifecycleKTX = "2.2.0"
    val retrofit = "2.8.1"
    val kotlin = "1.3.72"
    val androidMavenGradlePlugin = "2.1"
    val cropper = "2.8.0"
    val junit = "4.12"
    val testRunner = "1.2.0"
    val espressoCore = "3.2.0"
    val billing = "2.1.0"
    val navigation = "2.2.2"
    val coroutines = "1.3.3"
    val room = "2.2.5"
    val timber = "4.7.1"
}

object Libraries {
    val legacySupportV13 = "androidx.legacy:legacy-support-v13:${Versions.legacySupportV13}"

    val material = "com.google.android.material:material:${Versions.material}"

    val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"

    val playServicesAds = "com.google.android.gms:play-services-ads:${Versions.playServicesAds}"
    val consentLibrary = "com.google.android.ads.consent:consent-library:${Versions.consentLibrary}"

    val lifecycleExt = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
    val viewModelKTX = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    val liveDataKTX = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    val runtimeKTX = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleKTX}"

    val navigationFragment = "androidx.navigation:navigation-fragment:${Versions.navigation}"
    val navigationUi = "androidx.navigation:navigation-ui:${Versions.navigation}"

    val navigationFragmentKTX = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    val navigationUiKTX = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"

    val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"

    val gson = "com.google.code.gson:gson:${Versions.gson}"

    val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    val coreKTX = "androidx.core:core-ktx:${Versions.coreKTX}"
    val kotlinStdlibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"

    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val retrofitConverterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"

    val junit = "junit:junit:${Versions.junit}"
    val testRunner = "androidx.test:runner:${Versions.testRunner}"
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"

    val cropper = "com.theartofdev.edmodo:android-image-cropper:${Versions.cropper}"

    val billing = "com.android.billingclient:billing:${Versions.billing}"

    val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    val roomKTX = "androidx.room:room-ktx:${Versions.room}"

    val timber = "com.jakewharton.timber:timber:${Versions.timber}"
}

