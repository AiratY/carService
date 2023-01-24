package ru.airatyunusov.carservice.model

data class Customer(
    val id: Int,
    val name: String,
    val surname: String,
    val secondName: String,
    val listVar: List<CarModel>,

)
