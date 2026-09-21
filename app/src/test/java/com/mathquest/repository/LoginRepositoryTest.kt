package com.mathquest.repository

import com.mathquest.api.MathQuestApiService
import com.mathquest.model.LoginRequest
import com.mathquest.model.LoginResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Pruebas unitarias de [LoginRepository].
 *
 * [MathQuestApiService] y [SessionManager] se mockean con MockK: estas
 * pruebas validan la LOGICA del Repository (que interpreta la
 * respuesta HTTP y decide cuando persistir la sesion), no Retrofit ni
 * EncryptedSharedPreferences reales.
 */
class LoginRepositoryTest {

    private lateinit var apiService: MathQuestApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: LoginRepository

    private val email = "ana.torres@mathquest.edu"
    private val password = "MathQuest2026!"

    @Before
    fun setUp() {
        apiService = mockk()
        sessionManager = mockk(relaxUnitFun = true)
        repository = LoginRepository(sessionManager = sessionManager, apiService = apiService)
    }

    @Test
    fun `login exitoso guarda la sesion y devuelve Success`() = runTest {
        val loginResponse = LoginResponse(
            idUsuario = "11d7bb84-7003-4ba2-a584-ad230c560c69",
            nombre = "Ana Torres",
            rol = "alumno",
            tokenJwt = "token-jwt-de-prueba"
        )
        coEvery { apiService.login(LoginRequest(email = email, password = password)) } returns
            Response.success(loginResponse)

        val resultado = repository.login(email, password)

        assertTrue(resultado is LoginRepository.LoginResult.Success)
        assertEquals(
            loginResponse,
            (resultado as LoginRepository.LoginResult.Success).response
        )
        // El token + id_usuario deben persistirse SOLO en el camino exitoso.
        coVerify(exactly = 1) {
            sessionManager.saveSession(idUsuario = loginResponse.idUsuario, tokenJwt = loginResponse.tokenJwt)
        }
    }

    @Test
    fun `login con credenciales invalidas (401) devuelve Failure y no guarda sesion`() = runTest {
        val cuerpoError = "{\"error\":\"Credenciales inválidas.\"}"
            .toResponseBody("application/json".toMediaType())
        coEvery { apiService.login(LoginRequest(email = email, password = password)) } returns
            Response.error(401, cuerpoError)

        val resultado = repository.login(email, password)

        assertTrue(resultado is LoginRepository.LoginResult.Failure)
        coVerify(exactly = 0) { sessionManager.saveSession(any(), any()) }
    }

    @Test
    fun `login con fallo de red (IOException) devuelve Failure y no guarda sesion`() = runTest {
        coEvery { apiService.login(any()) } throws java.io.IOException("Sin conexión")

        val resultado = repository.login(email, password)

        assertTrue(resultado is LoginRepository.LoginResult.Failure)
        assertEquals(
            "No se pudo conectar con el servidor. Verifica tu conexión.",
            (resultado as LoginRepository.LoginResult.Failure).message
        )
        coVerify(exactly = 0) { sessionManager.saveSession(any(), any()) }
    }
}
