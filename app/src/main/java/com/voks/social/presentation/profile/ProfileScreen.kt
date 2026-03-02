package com.voks.social.presentation.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.voks.social.core.utils.Resource
import com.voks.social.presentation.home.PostCard
import com.voks.social.presentation.home.PostUiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfileState by viewModel.userProfile.collectAsState()
    val userPostsState by viewModel.userPosts.collectAsState()
    val updateProfileState by viewModel.updateProfileState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(updateProfileState) {
        if (updateProfileState is Resource.Success) {
            showEditDialog = false
            viewModel.resetUpdateState()
            Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
        } else if (updateProfileState is Resource.Error) {
            Toast.makeText(context, (updateProfileState as Resource.Error).message, Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (userProfileState is Resource.Success) {
                        Column {
                            Text(
                                text = (userProfileState as Resource.Success).data.username,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (userProfileState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(
                        text = (userProfileState as Resource.Error).message ?: "Error",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val user = (userProfileState as Resource.Success).data

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // Header: Banner y Avatar
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                // Banner
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .background(Color.LightGray)
                                ) {
                                    if (user.bannerUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = user.bannerUrl,
                                            contentDescription = "Banner",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                // Avatar flotante
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(start = 16.dp)
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                                ) {
                                    if (user.profilePictureUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = user.profilePictureUrl,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color.Gray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(user.username.take(1).uppercase(), color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // Info y Botón de Editar
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = { showEditDialog = true }) {
                                    Text("Editar Perfil")
                                }
                            }

                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${user.username.lowercase().replace(" ", "")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )

                                if (user.bio.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = user.bio, style = MaterialTheme.typography.bodyMedium)
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row {
                                    Text(text = "${user.following.size} ", fontWeight = FontWeight.Bold)
                                    Text(text = "Siguiendo", color = Color.Gray)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = "${user.followers.size} ", fontWeight = FontWeight.Bold)
                                    Text(text = "Seguidores", color = Color.Gray)
                                }
                            }
                            Divider(modifier = Modifier.padding(top = 16.dp))
                        }

                        // Feed del usuario
                        when (userPostsState) {
                            is Resource.Loading -> {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            is Resource.Success -> {
                                val posts = (userPostsState as Resource.Success).data
                                if (posts.isEmpty()) {
                                    item {
                                        Text(
                                            "No has publicado nada aún.",
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    items(posts) { post ->
                                        // Corregido: Pasamos el objeto 'user' completo como espera PostUiItem
                                        PostCard(
                                            postItem = PostUiItem(
                                                post = post,
                                                user = user
                                            )
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                item {
                                    Text("Error al cargar posts", color = Color.Red, modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }

                    if (showEditDialog) {
                        EditProfileDialog(
                            currentBio = user.bio,
                            currentAvatar = user.profilePictureUrl,
                            currentBanner = user.bannerUrl,
                            isLoading = updateProfileState is Resource.Loading,
                            onDismiss = { showEditDialog = false },
                            onSave = { newBio, avatarUri, bannerUri ->
                                viewModel.updateProfile(newBio, avatarUri, bannerUri)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentBio: String,
    currentAvatar: String,
    currentBanner: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Uri?, Uri?) -> Unit
) {
    var bioText by remember { mutableStateOf(currentBio) }
    var newAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var newBannerUri by remember { mutableStateOf<Uri?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> newAvatarUri = uri }
    )

    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> newBannerUri = uri }
    )

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Editar Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.DarkGray)
                        .clickable {
                            bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    AsyncImage(
                        model = newBannerUri ?: currentBanner,
                        contentDescription = "Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Icono corregido a Edit
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Cambiar Banner",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .clickable {
                            avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    AsyncImage(
                        model = newAvatarUri ?: currentAvatar,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Icono corregido a Edit
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Cambiar Avatar",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bioText,
                    onValueChange = { bioText = it },
                    label = { Text("Biografía") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isLoading) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(bioText, newAvatarUri, newBannerUri) },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}