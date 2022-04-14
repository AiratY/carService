package ru.airatyunusov.carservice.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TokenModel(
    //val id: Int,
    val startRecordDateTime: LocalDateTime,
    val endRecordDateTime: LocalDateTime,
    val idEmployee: Int
) {
    override fun toString(): String {
        return "${startRecordDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))} - " +
                "${endRecordDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))}"
    }
}