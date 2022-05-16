package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.CarModel
import ru.airatyunusov.carservice.model.User

interface CustomerCallBack {
    fun setListCars(listCars: List<CarModel>)
    fun setUser(user: User)
}
