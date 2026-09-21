package com.mathquest.viewmodel

import android.app.Application
import com.mathquest.model.LoginResponse
import com.mathquest.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias de [LoginViewModel] usando [UnconfinedTestDispatcher]
 * como dispatcher principal, para observar de forma deterministica y
 * sincronica la secuencia completa de estados que emite [LoginUiState]
 * (via StateFlow) al invocar [LoginViewModel.onLoginClick].
 *
 * [LoginRepository] se mockea con MockK: estas pruebas validan el
 * cableado ViewModel -> StateFlow, no la logica de red/persistencia
 * (eso ya lo cubre LoginRepositoryTest).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login exitoso transiciona Idle a Loading y luego a Success`() = runTest(testDispatcher) {
        val application = mockk<Application>(relaxed = true)
        val loginRepository = mockk<LoginRepository>()
        val loginResponse = LoginResponse(
            idUsuario = "11d7bb84-7003-4ba2-a584-ad230c560c69",
            nombre = "Ana Torres",
            rol = "alumno",
            tokenJwt = "token-jwt-de-prueba"
        )
        coEvery { loginRepository.login(any(), any()) } returns
            LoginRepository.LoginResult.Success(loginResponse)

        val viewModel = LoginViewModel(application = application, loginRepository = loginRepository)

        val estadosCapturados = mutableListOf<LoginUiState>()
        val job = launch { viewModel.uiState.collect { estadosCapturados.add(it) } }

        viewModel.onEmailChange("ana.torres@mathquest.edu")
        viewModel.onPasswordChange("MathQuest2026!")
        viewModel.onLoginClick()

        job.cancel()

        assertEquals(
            listOf(LoginUiState.Idle, LoginUiState.Loading, LoginUiState.Success),
            estadosCapturados
        )
    }

    @Test
    fun `login fallido transiciona Idle a Loading y luego a Error`() = runTest(testDispatcher) {
        val application = mockk<Application>(relaxed = true)
        val loginRepository = mockk<LoginRepository>()
        val mensajeError = "Credenciales inválidas o error del servidor (código 401)."
        coEvery { loginRepository.login(any(), any()) } returns
            LoginRepository.LoginResult.Failure(mensajeError)

        val viewModel = LoginViewModel(application = application, loginRepository = loginRepository)

        val estadosCapturados = mutableListOf<LoginUiState>()
        val job = launch { viewModel.uiState.collect { estadosCapturados.add(it) } }

        viewModel.onEmailChange("ana.torres@mathquest.edu")
        viewModel.onPasswordChange("clave-incorrecta")
        viewModel.onLoginClick()

        job.cancel()

        assertEquals(
            listOf(LoginUiState.Idle, LoginUiState.Loading, LoginUiState.Error(mensajeError)),
            estadosCapturados
        )
    }

    @Test
    fun `login con campos vacios va directo a Error sin pasar por Loading ni llamar al repository`() =
        runTest(testDispatcher) {
            val application = mockk<Application>(relaxed = true)
            val loginRepository = mockk<LoginRepository>()

            val viewModel = LoginViewModel(application = application, loginRepository = loginRepository)

            val estadosCapturados = mutableListOf<LoginUiState>()
            val job = launch { viewModel.uiState.collect { estadosCapturados.add(it) } }

            // Email/password quedan en blanco (valor inicial del ViewModel):
            // la validacion corta el flujo ANTES de llegar a Loading.
            viewModel.onLoginClick()

            job.cancel()

            assertEquals(
                listOf(LoginUiState.Idle, LoginUiState.Error("El correo y la contraseña son obligatorios")),
                estadosCapturados
            )
        }
}
