package ru.airatyunusov.carservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.model.ServiceModel

class EnrollFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enroll, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val carSpinner: Spinner = view.findViewById(R.id.carSpinner)
        val branchSpinner: Spinner = view.findViewById(R.id.branchSpinner)
        val serviceListRecyclerView: RecyclerView = view.findViewById(R.id.servicesListRecyclerView)
        val sendBtn: Button = view.findViewById(R.id.sendButton)

        val servicesListRecyclerViewAdapter = ServicesListRecyclerViewAdapter()
        servicesListRecyclerViewAdapter.setDataSet(LIST_SERVICES)

        serviceListRecyclerView.adapter = servicesListRecyclerViewAdapter

        val carList: Array<out String> =
            resources.getStringArray(R.array.carList) // arrayListOf("BMW", "LADA", "MERCEDES", "KIA")
        val branchList: Array<String> =
            arrayOf("СберСервис", "LADAСервис", "MERCEDESСервис", "KIAСервис")

        val carSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, carList
        )

        val branchSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, branchList
        )

        carSpinner.adapter = carSpinnerAdapter
        branchSpinner.adapter = branchSpinnerAdapter

        sendBtn.setOnClickListener {
            val list: List<ServiceModel> = servicesListRecyclerViewAdapter.getCheckedServices()
            val chooseBranch: String = branchSpinner.selectedItem.toString()
            val chooseCar = carSpinner.selectedItem.toString()
            transferDataOfEnroll(list, chooseBranch, chooseCar)
        }
    }

    private fun transferDataOfEnroll(listService: List<ServiceModel>, branch: String, car: String) {
        setFragmentResult(
            MainActivity.SHOW_SELECT_DATE_TIME,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                LIST_SERVICE to listService,
                BRANCH_SERVICES to branch,
                CAR to car
            )
        )
    }

    companion object {
        const val LIST_SERVICE = "list_services"
        const val BRANCH_SERVICES = "branch_services"
        const val CAR = "car"

        val LIST_SERVICES: List<ServiceModel> =
            listOf(
                ServiceModel(1, "Замена масла в ДВС", 1),
                ServiceModel(2, "Замена масляного фильтра", 4),
                ServiceModel(3, "Замена воздушного фильтра", 8),
                ServiceModel(4, "Замена топливного фильтра", 10),
                ServiceModel(5, "Сброс межсервисных интервалов", 2),
                ServiceModel(6, "Замена тормозной жидкости", 1),
                ServiceModel(7, "Замена патрубков системы охлаждения", 3),
                ServiceModel(8, "Замена жидкости ГУР", 7),
                ServiceModel(9, "Диагностика форсунок (диагностика инжектора)", 6),
            )
    }
    /*"Промывка инжектора",
    "Промывка форсунок",
    "Диагностика свечей зажигания",
    "Замена свечей зажигания",
    "Замена масла",
    "Чистка инжектора",*/
}
