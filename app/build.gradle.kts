plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // KSP genera el codigo de Room (DAO y base de datos) en tiempo de compilacion.
    alias(libs.plugins.ksp)
    // Plugin de Room: fija la carpeta de esquemas versionados para exportSchema.
    alias(libs.plugins.androidx.room)
}

room {
    // Los esquemas exportados (app/schemas) son la referencia para validar las
    // migraciones de la base local (RNF-14).
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "cl.cmq.salud"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.cmq.salud"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // Semana 9.3: captura con la camara del dispositivo (RF-09)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // LocalLifecycleOwner para enlazar la camara al ciclo de vida (Compose 1.10)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Persistencia local con Room (RNF-14): reemplaza el SQLiteOpenHelper manual.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    // Meta 8: pruebas de ViewModels con corrutinas (Dispatchers.Main simulado)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    // Pruebas de la capa Room sobre SQLite en memoria (JVM con Robolectric no incluido:
    // las pruebas de DAO viven en androidTest).
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}