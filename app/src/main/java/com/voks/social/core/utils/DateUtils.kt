package com.voks.social.core.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Convierte una fecha en formato ISO-8601 (el que devuelve Appwrite)
 * a un formato de tiempo relativo estilo X (Twitter).
 * Ejemplo: "Ahora", "5m", "2h", "1d", "3 mar".
 */
fun formatRelativeTime(createdAt: String): String {
    if (createdAt.isBlank()) return ""

    return try {
        // Parseamos el string que envía Appwrite
        val instant = Instant.parse(createdAt)
        val postTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        val now = ZonedDateTime.now(ZoneId.systemDefault())

        // Calculamos las diferencias de tiempo
        val minutes = ChronoUnit.MINUTES.between(postTime, now)
        val hours = ChronoUnit.HOURS.between(postTime, now)
        val days = ChronoUnit.DAYS.between(postTime, now)

        // Formateamos según la diferencia
        when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> {
                // Si es más de una semana, mostramos la fecha (ej. "3 mar")
                val formatter = DateTimeFormatter.ofPattern("d MMM")
                postTime.format(formatter)
            }
        }
    } catch (e: Exception) {
        // En caso de error al parsear, devolvemos el string original como fallback
        createdAt
    }
}