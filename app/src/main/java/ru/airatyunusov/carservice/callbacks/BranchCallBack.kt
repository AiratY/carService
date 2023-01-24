package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.Employee

interface BranchCallBack {
    fun setListEmployee(listEmployee: List<Employee>)
}
