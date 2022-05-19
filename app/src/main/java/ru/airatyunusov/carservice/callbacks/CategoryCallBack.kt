package ru.airatyunusov.carservice.callbacks

import ru.airatyunusov.carservice.model.CategoryServices

interface CategoryCallBack {
    fun setListCategories(list: List<CategoryServices>)
}
