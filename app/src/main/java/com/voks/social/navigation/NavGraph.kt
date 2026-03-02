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
import com.voks.social.presentation.home.HomeScreen
import com.voks.social.presentation.post.CreatePostScreen
import com.voks.social.presentation.profile.ProfileScreen // AÑADIDO

@Composable
fun NavGraph(startDestination: String = Screen.Welcome.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToVerification = {
                    navController.navigate(Screen.Verification.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Verification.route) {
            VerificationScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToCreatePost = { navController.navigate(Screen.CreatePost.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }, // AÑADIDO
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // NUEVO FASE 10: Ruta del perfil (le pasamos el navController completo temporalmente)
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                navController = navController
            )
        }
    }
}