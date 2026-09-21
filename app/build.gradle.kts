// Build file de nivel app para el modulo MathQuest.
//
// NOTA: este proyecto todavia no incluye settings.gradle.kts ni el
// build.gradle.kts raiz (se agregaran en una iteracion posterior de
// scaffolding de Gradle); este archivo asume que las versiones de los
// plugins de Android/Kotlin se resuelven desde el proyecto raiz, como es
// habitual en un proyecto generado por Android Studio.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mathquest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mathquest"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // MainActivity extiende FragmentActivity (requisito de BiometricPrompt
    // para poder mostrar el dialogo biometrico del sistema).
    implementation("androidx.fragment:fragment-ktx:1.8.1")

    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Icons.Default.* (usado por el FAB de DashboardView) vive en este
    // artefacto separado; material3 no lo trae de forma transitiva.
    implementation("androidx.compose.material:material-icons-core")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Autenticacion biometrica (huella/rostro) mediante el sistema
    // biometrico seguro del OS (BiometricPrompt + Keystore/TEE). La app
    // nunca captura, procesa ni almacena datos biometricos: solo recibe
    // un resultado de exito/fallo/error desde el sistema operativo.
    implementation("androidx.biometric:biometric:1.1.0")

    // Capa de red (Sprint 4): Retrofit + Gson para consumir la API REST
    // de MathQuest (login, progreso, etc.) desde la capa Repository.
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // OkHttp se declara explicitamente (ya llega transitivo via Retrofit)
    // porque RetrofitClient.kt importa clases okhttp3.* directamente
    // (Interceptor/OkHttpClient) para inyectar el header Authorization.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Necesarios para lanzar las llamadas de red suspendidas desde
    // viewModelScope (LoginViewModel) en el dispatcher principal de Android.
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Almacenamiento cifrado (SessionManager) para persistir el token_jwt
    // usando EncryptedSharedPreferences respaldado por el Android Keystore.
    // NOTA: la libreria aun no tiene una version 1.x estable "final"; se
    // usa el ultimo alpha de la serie 1.1, ampliamente usado en produccion
    // para EncryptedSharedPreferences/EncryptedFile.
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
