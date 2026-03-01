package com.voks.social.presentation.post

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.voks.social.core.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: PostViewModel = hiltViewModel()
) {
    var content by remember { mutableStateOf("") }
    val postState by viewModel.createPostState.collectAsState()
    val selectedMediaUri by viewModel.selectedMediaUri.collectAsState()

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            viewModel.setMediaUri(uri)
        }
    )

    LaunchedEffect(postState) {
        when (postState) {
            is Resource.Success -> {
                viewModel.resetCreatePostState()
                onNavigateBack()
            }
            is Resource.Error -> {
                Toast.makeText(context, (postState as Resource.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetCreatePostState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    val isLoading = postState is Resource.Loading
                    val isEnabled = (content.isNotBlank() || selectedMediaUri != null) && !isLoading

                    Button(
                        onClick = { viewModel.createPost(content) },
                        enabled = isEnabled,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Publicar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("¿Qué está pasando?") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            if (selectedMediaUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    AsyncImage(
                        model = selectedMediaUri,
                        contentDescription = "Vista previa multimedia",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { viewModel.setMediaUri(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar multimedia",
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adjuntar multimedia",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}