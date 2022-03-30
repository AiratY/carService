package ru.airatyunusov.carservice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import ru.airatyunusov.carservice.model.ServiceModel

class SelectDateTimeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_date_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val listServiceModel: List<ServiceModel> =
                it.get(LIST_SERVICE) as? List<ServiceModel> ?: emptyList()

            val countHours = getHoursAllServices(listServiceModel)
            getListEnrollForServices(countHours)
        }
    }
    /*
    * Функция запроса данных с БД
    * Обработка этих данных
    * Выдача талонов
    *
    */
    private fun getListEnrollForServices(countHours: Int) {

    }

    private fun getHoursAllServices(listServiceModel: List<ServiceModel>): Int {
        var countHours = 0
        for (service in listServiceModel){
            countHours += service.hours
        }
        return countHours
    }

    companion object {
        private const val LIST_SERVICE = "list_services"
        private const val BRANCH_SERVICES = "branch_services"
        private const val CAR = "car"

        fun newInstance(list: List<ServiceModel>): SelectDateTimeFragment {
            return SelectDateTimeFragment().apply {
                arguments = bundleOf(LIST_SERVICE to list)
            }
        }
    }
}