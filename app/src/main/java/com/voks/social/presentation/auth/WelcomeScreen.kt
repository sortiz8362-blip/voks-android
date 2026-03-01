package com.voks.social.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo Tipográfico de voks
            Text(
                text = "voks",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 72.sp,
                    letterSpacing = (-3).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lo que está pasando ahora.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón de Crear Cuenta
            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Crear cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Separador visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
                Text(
                    text = "o",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Iniciar Sesión
            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text("Iniciar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}