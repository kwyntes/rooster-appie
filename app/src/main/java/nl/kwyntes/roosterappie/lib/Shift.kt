package nl.kwyntes.roosterappie.lib

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM")
val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

enum class AuthorisedStatus {
    Authorised,
    NotAuthorised,
    None
}

data class Shift(
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val authorisedStatus: AuthorisedStatus
) {
    fun formatDate() =
        when (date.dayOfWeek.value) {
            1 -> "maandag"
            2 -> "dinsdag"
            3 -> "woensdag"
            4 -> "donderdag"
            5 -> "vrijdag"
            6 -> "zaterdag"
            7 -> "zondag"
            else -> ""
        } + " " + date.format(DATE_FORMATTER)

    fun formatTimeFrame() = "${start.format(TIME_FORMATTER)} - ${end.format(TIME_FORMATTER)}"
}
