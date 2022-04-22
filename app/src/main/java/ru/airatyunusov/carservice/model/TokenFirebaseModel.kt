package ru.airatyunusov.carservice.model

class TokenFirebaseModel() {
    var startRecordDateTime: String = ""
    var endRecordDateTime: String = ""
    var idEmployee: Int = 0

    constructor(
        startRecordDateTime: String,
        endRecordDateTime: String,
        idEmployee: Int
    ) : this() {
        this.startRecordDateTime = startRecordDateTime
        this.endRecordDateTime = endRecordDateTime
        this.idEmployee = idEmployee
    }
}
