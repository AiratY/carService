package ru.airatyunusov.carservice.model

data class TokenFirebaseModel(
    var id: String = "",
    var startRecordDateTime: String = "",
    var endRecordDateTime: String = "",
    val idEmployee: String = "",
    var listServices: List<ServiceModel> = emptyList()
)
