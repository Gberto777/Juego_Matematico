package com.mathquest.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestiona la persistencia segura de la sesion (token JWT) usando
 * [EncryptedSharedPreferences].
 *
 * El token se cifra en disco con una clave maestra respaldada por el
 * Android Keystore ([MasterKey]), por lo que nunca queda guardado en
 * texto plano en el almacenamiento del dispositivo. Esta clase NUNCA
 * guarda la contraseña del usuario ni datos biometricos, unicamente el
 * `token_jwt` devuelto por el backend.
 *
 * Se recomienda construirla con el Application context (no un
 * Activity/Fragment context) para evitar leaks; ver su uso en
 * [com.mathquest.viewmodel.LoginViewModel] (AndroidViewModel).
 */
class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /** Guarda (o sobrescribe) el token JWT de la sesion actual, cifrado. */
    fun saveToken(tokenJwt: String) {
        encryptedPrefs.edit().putString(KEY_TOKEN_JWT, tokenJwt).apply()
    }

    /** Devuelve el token JWT persistido, o null si no hay sesion guardada. */
    fun getToken(): String? = encryptedPrefs.getString(KEY_TOKEN_JWT, null)

    /** True si hay un token de sesion persistido. */
    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    /** Elimina la sesion persistida (logout). */
    fun clearSession() {
        encryptedPrefs.edit().remove(KEY_TOKEN_JWT).apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "mathquest_secure_prefs"
        private const val KEY_TOKEN_JWT = "token_jwt"
    }
}
