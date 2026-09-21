package com.mathquest.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.mathquest.repository.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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
 * Requiere llamar a [init] una vez antes del primer uso de
 * [apiService] (ver [com.mathquest.MathQuestApplication.onCreate]),
 * para que el interceptor de autenticacion tenga acceso al
 * [SessionManager] y pueda inyectar el header
 * `Authorization: Bearer <token>` en cada peticion.
 *
 * TODO(Sprint 6+): mover BASE_URL a BuildConfig / variantes de build
 * (debug vs release) en lugar de una constante fija, cuando exista un
 * backend real desplegado.
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"

    private var sessionManager: SessionManager? = null

    /**
     * Inicializa el cliente con el Application context. Seguro de
     * llamar mas de una vez; solo la primera llamada tiene efecto.
     */
    fun init(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }

    /**
     * Interceptor de OkHttp que lee el token JWT persistido en
     * [SessionManager] e inyecta la cabecera `Authorization: Bearer
     * <token>` en cada peticion. Si no hay token guardado (usuario aun
     * no autenticado, ej. la propia llamada de login), la peticion
     * sigue sin la cabecera; las rutas protegidas del backend
     * responderan 401 en ese caso.
     */
    private val authInterceptor = Interceptor { chain ->
        val token = sessionManager?.getToken()
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    private val gson = GsonBuilder().create()

    val apiService: MathQuestApiService by lazy {
        checkNotNull(sessionManager) {
            "RetrofitClient.init(context) debe llamarse antes de usar apiService " +
                "(ver MathQuestApplication.onCreate)."
        }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MathQuestApiService::class.java)
    }
}
