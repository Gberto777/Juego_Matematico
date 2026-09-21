package com.mathquest.ui.login

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Encapsula el uso de [BiometricPrompt] para el desbloqueo biometrico de
 * MathQuest.
 *
 * Seguridad: esta clase NUNCA lee, procesa ni almacena datos biometricos
 * (huellas/rostro) dentro de la app. [BiometricPrompt] delega toda la
 * captura y verificacion en el sistema biometrico seguro del sistema
 * operativo (Keystore / Trusted Execution Environment); la aplicacion
 * solo recibe como resultado un exito, un fallo (huella no reconocida) o
 * un error (hardware no disponible, sin huellas enroladas, cancelacion,
 * etc.), nunca la huella en si.
 */
object BiometricAuthenticator {

    /**
     * True si el dispositivo tiene hardware biometrico disponible y al
     * menos una huella/rostro enrolado con el sistema.
     */
    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Muestra el prompt biometrico nativo del sistema operativo.
     *
     * @param activity FragmentActivity que actua como host del dialogo
     *   (requisito de BiometricPrompt).
     * @param onSuccess invocado cuando el sistema valida la huella/rostro
     *   como autentica.
     * @param onFailure invocado cuando la biometria es invalida (huella
     *   no reconocida), cuando el usuario elige el boton "Usar
     *   contraseña", o cuando ocurre cualquier otro error (hardware no
     *   disponible, sin huellas enroladas, demasiados intentos, etc.).
     *   En todos estos casos el flujo debe forzar al usuario a
     *   autenticarse con el input de contraseña.
     */
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!canAuthenticate(activity)) {
            onFailure("El desbloqueo biométrico no está disponible en este dispositivo. Usa tu contraseña.")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            // Huella/rostro leido pero no reconocido como valido. El
            // prompt del sistema permite reintentar; igualmente
            // notificamos el fallo para que la UI recuerde el metodo
            // alternativo de contraseña.
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailure("Huella no reconocida. Inténtalo de nuevo o usa tu contraseña.")
            }

            // Errores irrecuperables para este intento: hardware no
            // disponible, sin biometria enrolada, demasiados intentos
            // fallidos, o el usuario presiono "Usar contraseña"
            // (ERROR_NEGATIVE_BUTTON). En todos los casos se cae al
            // metodo alternativo de password.
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFailure(errString.toString())
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloqueo biométrico")
            .setSubtitle("Usa tu huella o rostro para iniciar sesión en MathQuest")
            .setNegativeButtonText("Usar contraseña")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
