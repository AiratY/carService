package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.CarModel
import ru.airatyunusov.carservice.model.TokenFirebaseModel

interface CustomerCallBack {
    fun setListCars(listCars: List<CarModel>)
    fun setListToken(listToken: List<TokenFirebaseModel>)
}