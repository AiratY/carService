package ru.airatyunusov.carservice.model

data class Employee(
    val id: Int,
    val name: String,
    val surname: String,
    val secondName: String,
) {
    override fun toString(): String {
        return "$surname ${name[0]}.${secondName[0]}."
    }
}