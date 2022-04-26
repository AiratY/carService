package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.ServiceModel

interface ServicesCallBack {
    fun setListServices(list: List<ServiceModel>)
}
