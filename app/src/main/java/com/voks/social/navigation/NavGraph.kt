package com.voks.social.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voks.social.presentation.auth.ForgotPasswordScreen
import com.voks.social.presentation.auth.LoginScreen
import com.voks.social.presentation.auth.RegisterScreen
import com.voks.social.presentation.auth.VerificationScreen
import com.voks.social.presentation.auth.WelcomeScreen

// AHORA LA PANTALLA DE INICIO ES WELCOME
@Composable
fun NavGraph(startDestination: String = Screen.Welcome.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        // --- PANTALLA DE BIENVENIDA ---
        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        // --- LOGIN ---
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToHome = { /* TODO: Navegaremos al Feed en la Fase 7 */ },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        // --- REGISTRO ---
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToVerification = { navController.navigate(Screen.Verification.route) },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // --- VERIFICACIÓN ---
        composable(route = Screen.Verification.route) {
            VerificationScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        // --- OLVIDÉ CONTRASEÑA ---
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}