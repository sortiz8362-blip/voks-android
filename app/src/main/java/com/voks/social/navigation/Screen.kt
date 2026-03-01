package com.voks.social.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Verification : Screen("verification") // Agregado para Fase 4
    object Home : Screen("home")
}