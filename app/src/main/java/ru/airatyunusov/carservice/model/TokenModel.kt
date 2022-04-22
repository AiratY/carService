package ru.airatyunusov.carservice.model

import ru.airatyunusov.carservice.DateTimeHelper
import java.time.LocalDateTime

data class TokenModel(
    // val id: Int,
    val startRecordDateTime: LocalDateTime,
    val endRecordDateTime: LocalDateTime,
    val idEmployee: Int,
    // val listServices: List<ServiceModel>
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
