package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.ServiceModel

interface TestCallBack {
    fun setListBranch(list: List<BranchModel>)
    fun setListServices(listServices: List<ServiceModel>)
}
