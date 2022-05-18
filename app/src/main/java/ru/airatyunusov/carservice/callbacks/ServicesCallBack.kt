package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.CategoryServices
import ru.airatyunusov.carservice.model.ServiceModel
import java.lang.ref.WeakReference

interface ServicesCallBack {
    fun setListServices(listServices: List<ServiceModel>)
    fun setListCategoriesServices(listCategory: List<CategoryServices>, weakReference: WeakReference<ServicesCallBack>)
}
