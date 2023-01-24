package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.CategoryServices
import ru.airatyunusov.carservice.model.ServiceModel

interface EnrollCustomerCallBack {
    fun setListBranch(list: List<BranchModel>)
    fun setListServices(listServices: List<ServiceModel>)
    fun setListCategories(list: List<CategoryServices>)
}
