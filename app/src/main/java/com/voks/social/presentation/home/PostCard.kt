package com.voks.social.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.voks.social.core.utils.formatRelativeTime

@Composable
fun PostCard(
    postItem: PostUiItem,
    onPostClick: (String) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onLikeClick: (String) -> Unit = {},
    onBookmarkClick: (String) -> Unit = {},
    onCommentClick: (String) -> Unit = {},
    onRepostClick: (String) -> Unit = {} // NUEVO FASE 15
) {
    val rawPost = postItem.post
    val rawUser = postItem.user

    // FASE 15: Determinar si es un Repost puro (Sin texto añadido). Si lo es, mostramos el post original
    val isPureRepost = rawPost.originalPostId.isNotEmpty() && rawPost.content.isEmpty()
    val displayPost = if (isPureRepost && postItem.originalPost != null) postItem.originalPost else rawPost
    val displayUser = if (isPureRepost && postItem.originalPostUser != null) postItem.originalPostUser else rawUser

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick(displayPost.id) }
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
    ) {
        // Cabecera indicadora de Repost
        if (isPureRepost) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repost",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${rawUser?.username ?: "Alguien"} reposteó",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onUserClick(displayPost.userId) },
                contentAlignment = Alignment.Center
            ) {
                if (!displayUser?.profilePictureUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = displayUser?.profilePictureUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido del Post
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Cabecera de usuario
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = displayUser?.username ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onUserClick(displayPost.userId) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@${displayUser?.username ?: "usuario"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "·", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatRelativeTime(displayPost.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Texto principal
                if (displayPost.content.isNotEmpty()) {
                    Text(
                        text = displayPost.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Imagen adjunta principal (Si existe)
                if (displayPost.imageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = displayPost.imageUrl,
                        contentDescription = "Imagen del post",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                }

                // FASE 15: Post Citado (Quote Tweet - Un post dentro de otro post)
                if (!isPureRepost && rawPost.originalPostId.isNotEmpty() && postItem.originalPost != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPostClick(rawPost.originalPostId) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = postItem.originalPostUser?.profilePictureUrl?.ifEmpty { "https://ui-avatars.com/api/?name=${postItem.originalPostUser.username}" },
                                    contentDescription = "Avatar citado",
                                    modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.Gray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = postItem.originalPostUser?.username ?: "Usuario",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "· ${formatRelativeTime(postItem.originalPost.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (postItem.originalPost.content.isNotEmpty()) {
                                Text(
                                    text = postItem.originalPost.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (postItem.originalPost.imageUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = postItem.originalPost.imageUrl,
                                    contentDescription = "Imagen citada",
                                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // Barra de Interacciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        // Comentarios
                        IconButton(onClick = { onCommentClick(displayPost.id) }) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Comentar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // FASE 15: Repost Button
                        IconButton(onClick = { onRepostClick(displayPost.id) }) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repostear",
                                tint = if (postItem.isRepostedByMe) Color(0xFF00BA7C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (displayPost.reposts.isNotEmpty()) {
                            Text(
                                text = displayPost.reposts.size.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (postItem.isRepostedByMe) Color(0xFF00BA7C) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Likes
                        IconButton(onClick = { onLikeClick(displayPost.id) }) {
                            Icon(
                                imageVector = if (postItem.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Me gusta",
                                tint = if (postItem.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (displayPost.likes.isNotEmpty()) {
                            Text(
                                text = displayPost.likes.size.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (postItem.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Bookmark
                    IconButton(onClick = { onBookmarkClick(displayPost.id) }) {
                        Icon(
                            imageVector = if (postItem.isBookmarkedByMe) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Guardar",
                            tint = if (postItem.isBookmarkedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
}