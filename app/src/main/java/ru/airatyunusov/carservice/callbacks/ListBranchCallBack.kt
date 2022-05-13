package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.BranchModel

interface ListBranchCallBack {
    fun setListBranch(list: List<BranchModel>)
}