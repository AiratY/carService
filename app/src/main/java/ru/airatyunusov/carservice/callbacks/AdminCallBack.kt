package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.BranchModel

interface AdminCallBack {
    fun setListBranch(listBranchs: List<BranchModel>)

    fun onClickBranchs(branch: BranchModel)
}
