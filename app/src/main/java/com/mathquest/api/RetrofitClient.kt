package com.mathquest.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Punto unico de acceso a Retrofit para MathQuest (patron Singleton).
 *
 * BASE_URL usa `10.0.2.2`, la direccion especial que el emulador de
 * Android redirige al `localhost` de la maquina host, para apuntar a un
 * backend de desarrollo corriendo localmente (ej. `npm run dev` en el
 * puerto 3000). En un dispositivo fisico o para producción, este valor
 * debe reemplazarse por la URL real del backend desplegado.
 *
 * TODO(Sprint 5+): mover BASE_URL a BuildConfig / variantes de build
 * (debug vs release) en lugar de una constante fija, cuando exista un
 * backend real desplegado.
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val gson = GsonBuilder().create()

    val apiService: MathQuestApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MathQuestApiService::class.java)
    }
}
