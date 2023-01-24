package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.TokenFirebaseModel

interface ListTokenCallBack {
    fun setListTokenFirebaseModel(listToken: List<TokenFirebaseModel>)
}
