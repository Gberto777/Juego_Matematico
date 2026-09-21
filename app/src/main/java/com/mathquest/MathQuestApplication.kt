package com.mathquest

import android.app.Application
import com.mathquest.api.RetrofitClient

/**
 * Application class de MathQuest.
 *
 * Unicamente inicializa [RetrofitClient] con el Application context tan
 * pronto como arranca el proceso, para que el interceptor de
 * autenticacion (que lee el token via SessionManager) este listo antes
 * de que cualquier Activity/ViewModel realice la primera peticion de
 * red.
 */
class MathQuestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
