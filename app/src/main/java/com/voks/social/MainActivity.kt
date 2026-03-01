package com.voks.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.voks.social.core.utils.Resource
import com.voks.social.navigation.NavGraph
import com.voks.social.navigation.Screen
import com.voks.social.presentation.auth.AuthViewModel
import com.voks.social.ui.theme.VoksTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoksTheme {
                // Inyectamos el ViewModel a nivel de actividad
                val authViewModel: AuthViewModel = hiltViewModel()
                val authStatus by authViewModel.isUserLoggedIn.collectAsState()

                // Lanzamos la comprobación de sesión al abrir la app
                LaunchedEffect(Unit) {
                    authViewModel.checkAuthStatus()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (authStatus) {
                        // Mientras comprueba, mostramos un indicador de carga (Splash temporal)
                        is Resource.Loading, null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is Resource.Success -> {
                            // Si tiene sesión activa va a Home, si no, va a Login
                            val isLoggedIn = (authStatus as Resource.Success<Boolean>).data
                            val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
                            val navController = rememberNavController()

                            NavGraph(navController = navController, startDestination = startDestination)
                        }
                        is Resource.Error -> {
                            // Ante cualquier error, mandamos al Login por seguridad
                            val navController = rememberNavController()
                            NavGraph(navController = navController, startDestination = Screen.Login.route)
                        }
                    }
                }
            }
        }
    }
}