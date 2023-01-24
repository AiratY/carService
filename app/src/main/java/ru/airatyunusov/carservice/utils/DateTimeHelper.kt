package ru.airatyunusov.carservice.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeHelper {
    companion object {
        private const val PATTERN = "dd-MM-yyyy HH:mm"

        private fun getDateTimeFormatter(): DateTimeFormatter? {
            return DateTimeFormatter.ofPattern(PATTERN)
        }

        fun convertToStringMyPattern(dateTime: LocalDateTime): String {
            return dateTime.format(getDateTimeFormatter())
        }

        fun convertToLocalDateTime(dateTime: String): LocalDateTime {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME)
        }

        fun convertToStringDateTime(dateTime: LocalDateTime): String {
            return dateTime.format(DateTimeFormatter.ISO_DATE_TIME)
        }

        /*private fun convertToLong(dateTime: LocalDateTime): Long {
            val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
            return zonedDateTime.toInstant().toEpochMilli()
        }

        private fun convertToLocalDateTime(epochMilli: Long): LocalDateTime {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault())
        }*/
    }
}
