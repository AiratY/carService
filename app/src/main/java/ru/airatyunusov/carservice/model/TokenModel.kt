package ru.airatyunusov.carservice.model

import ru.airatyunusov.carservice.utils.DateTimeHelper
import java.time.LocalDateTime

data class TokenModel(
    val startRecordDateTime: LocalDateTime = LocalDateTime.now(),
    val endRecordDateTime: LocalDateTime = LocalDateTime.now(),
    val idEmployee: String = ""
) {
    override fun toString(): String {
        return "${getStringStartDateTime()} - " + getStringEndDateTime()
    }

    private fun getStringStartDateTime(): String {
        return DateTimeHelper.convertToStringMyPattern(startRecordDateTime)
    }

    private fun getStringEndDateTime(): String {
        return DateTimeHelper.convertToStringMyPattern(endRecordDateTime)
    }
}
