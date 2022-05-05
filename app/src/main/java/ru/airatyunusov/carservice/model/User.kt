package ru.airatyunusov.carservice.model

data class User(
    var id: String = "",
    var role: String = "",
    var name: String = "",
    var phone: Long = 0,
) {
}