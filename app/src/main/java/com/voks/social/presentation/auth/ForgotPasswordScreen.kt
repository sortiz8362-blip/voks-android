package com.voks.social.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voks.social.core.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val recoveryState by viewModel.passwordRecoveryState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Encuentra tu cuenta de voks",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ingresa el correo electrónico asociado a tu cuenta para recibir un enlace de recuperación de contraseña.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                // CORRECCIÓN: Uso de OutlinedTextFieldDefaults
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.sendPasswordRecoveryEmail(email.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                enabled = email.isNotBlank() && recoveryState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (recoveryState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.background,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar enlace", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (recoveryState is Resource.Success) {
                Text(
                    text = "Enlace enviado. Por favor revisa tu correo.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else if (recoveryState is Resource.Error) {
                Text(
                    text = (recoveryState as Resource.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Volver a Iniciar sesión",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}