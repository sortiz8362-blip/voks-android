package com.voks.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.voks.social.core.utils.Resource
import com.voks.social.navigation.NavGraph
import com.voks.social.navigation.Screen
import com.voks.social.presentation.auth.AuthViewModel
import com.voks.social.ui.theme.VoksTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SplashScreen: esperamos a que se resuelva la consulta del usuario
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                authViewModel.userState.value is Resource.Loading
            }
        }

        enableEdgeToEdge()

        // Fase 4: Validamos los datos completos del usuario para ver si su email está verificado
        authViewModel.checkUser()

        setContent {
            VoksTheme {
                val userState by authViewModel.userState.collectAsState()

                // Determinamos a qué pantalla ir basándonos en si hay sesión y si está verificado
                val startDestination = when (userState) {
                    is Resource.Success -> {
                        val isVerified = (userState as Resource.Success).data.emailVerification
                        if (isVerified) Screen.Home.route else Screen.Verification.route
                    }
                    // AHORA: Si hay error (no hay sesión), lo mandamos a Welcome
                    is Resource.Error -> Screen.Welcome.route
                    else -> Screen.Welcome.route
                }

                // Solo renderizamos la navegación si ya obtuvimos una respuesta
                if (userState !is Resource.Loading && userState != null) {
                    NavGraph(startDestination = startDestination)
                } else if (userState is Resource.Error || userState == null) {
                    // AHORA: Fallback de seguridad a Welcome
                    NavGraph(startDestination = Screen.Welcome.route)
                }
            }
        }
    }
}