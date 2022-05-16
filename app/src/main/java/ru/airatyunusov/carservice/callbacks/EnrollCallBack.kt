package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.TokenModel

interface EnrollCallBack {
    fun setListEmployeeAndNewToken(listEmployee: List<Employee>, listNewToken: List<TokenModel>)
    fun showMessage()
}
