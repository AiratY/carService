package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.TokenFirebaseModel

interface EmployeePageCallBack {
    fun setListTokenFirebaseModel(list: List<TokenFirebaseModel>)
    fun setDataEmployee(employee: Employee)
}
