package com.voks.social.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Verification : Screen("verification")
    object ForgotPassword : Screen("forgot_password") // NUEVA RUTA
    object Home : Screen("home")
}