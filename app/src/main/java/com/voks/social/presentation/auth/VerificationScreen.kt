package com.voks.social.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voks.social.core.utils.Resource

@Composable
fun VerificationScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val userState by viewModel.userState.collectAsState()
    val verificationState by viewModel.verificationState.collectAsState()

    // Al entrar a la pantalla, revisamos el estado actual del usuario
    LaunchedEffect(Unit) {
        viewModel.checkUser()
    }

    // Escuchamos activamente si el usuario ya verificó su cuenta
    LaunchedEffect(userState) {
        if (userState is Resource.Success) {
            val user = (userState as Resource.Success).data
            if (user.emailVerification) {
                onNavigateToHome()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verifica tu correo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Te hemos enviado un enlace mágico a tu correo electrónico. Haz clic en él para verificar tu cuenta de voks.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.checkUser() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ya lo verifiqué")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.sendVerificationEmail() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reenviar correo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                viewModel.logout()
                onNavigateToLogin()
            }
        ) {
            Text("Cerrar sesión")
        }

        // Feedback del estado del envío
        when (verificationState) {
            is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            is Resource.Success -> Text(
                "¡Correo enviado con éxito!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
            is Resource.Error -> Text(
                "Error: ${(verificationState as Resource.Error).message}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
            else -> {}
        }
    }
}