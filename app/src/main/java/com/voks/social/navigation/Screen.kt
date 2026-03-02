package com.voks.social.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ForgotPassword : Screen("forgot_password")
    object Verification : Screen("verification")

    // FASE 15: Modificamos CreatePost para aceptar un argumento opcional "quoteId"
    object CreatePost : Screen("create_post?quoteId={quoteId}") {
        fun createRoute(quoteId: String? = null): String {
            return if (quoteId != null) "create_post?quoteId=$quoteId" else "create_post"
        }
    }

    object Profile : Screen("profile?userId={userId}") {
        fun createRoute(userId: String? = null): String {
            return if (userId != null) "profile?userId=$userId" else "profile"
        }
    }

    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String): String {
            return "post_detail/$postId"
        }
    }
}