package ru.airatyunusov.carservice.model

class Employee() {
    var id: Int = 0
    var firstName: String = ""
    var lastName: String = ""
    var patronymic: String = ""

    constructor(
        id: Int,
        firstName: String,
        lastName: String,
        patronymic: String,
    ) : this() {
        this.id = id
        this.firstName = firstName
        this.lastName = lastName
        this.patronymic = patronymic
    }

    override fun toString(): String {
        return "$lastName ${firstName[0]}.${patronymic[0]}."
    }
}
