package com.voks.social.presentation.post

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voks.social.core.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: PostViewModel = hiltViewModel()
) {
    var postText by remember { mutableStateOf("") }
    val createPostState by viewModel.createPostState.collectAsState()
    val context = LocalContext.current
    val maxChars = 280

    // Escuchamos los cambios de estado (Éxito, Error)
    LaunchedEffect(createPostState) {
        when (createPostState) {
            is Resource.Success -> {
                Toast.makeText(context, "Publicado correctamente", Toast.LENGTH_SHORT).show()
                viewModel.resetCreatePostState()
                onNavigateBack() // Regresamos al feed (Home) al publicar con éxito
            }
            is Resource.Error -> {
                val errorMessage = (createPostState as Resource.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetCreatePostState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.createPost(postText) },
                        // Deshabilitar si está vacío, se pasa del límite, o está cargando
                        enabled = postText.isNotBlank() && postText.length <= maxChars && createPostState !is Resource.Loading,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Publicar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TextField(
                    value = postText,
                    onValueChange = { if (it.length <= maxChars) postText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Toma todo el espacio restante disponible
                    placeholder = { Text("¿Qué está pasando?") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Contador de caracteres
                Text(
                    text = "${postText.length} / $maxChars",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (postText.length == maxChars) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Indicador de carga
            if (createPostState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}