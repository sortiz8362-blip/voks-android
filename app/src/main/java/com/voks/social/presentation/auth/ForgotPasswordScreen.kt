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
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val recoveryState by viewModel.passwordRecoveryState.collectAsState()

    // Opcional: limpiar el estado al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose { viewModel.clearStates() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Recuperar Contraseña", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ingresa el correo electrónico asociado a tu cuenta y te enviaremos un enlace para restablecer tu contraseña.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.sendPasswordRecoveryEmail(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() && recoveryState !is Resource.Loading
        ) {
            if (recoveryState is Resource.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Enviar enlace de recuperación")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar feedback al usuario
        when (recoveryState) {
            is Resource.Success -> {
                Text(
                    text = "¡Correo enviado! Revisa tu bandeja de entrada.",
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            is Resource.Error -> {
                Text(
                    text = "Error: ${(recoveryState as Resource.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Volver a Iniciar Sesión")
        }
    }
}