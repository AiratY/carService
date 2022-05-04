package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.CarModel
import ru.airatyunusov.carservice.model.Employee

interface TokenCallBack {
    fun setCar(car: CarModel)
    fun setEmployee(employee: Employee)
    fun setBranch(branch: BranchModel)
}