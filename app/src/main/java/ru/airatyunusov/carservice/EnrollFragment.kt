package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.callbacks.EnrollCustomerCallBack
import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.CarModel
import ru.airatyunusov.carservice.model.CategoryServices
import ru.airatyunusov.carservice.model.ServiceModel
import java.lang.ref.WeakReference

class EnrollFragment : BaseFragment(), EnrollCustomerCallBack {

    private val callBack: EnrollCustomerCallBack = this
    private var branchSpinner: Spinner? = null
    private var servicesListRecyclerViewAdapter: ServicesListRecyclerViewAdapter? = null
    private var listCars: List<CarModel>? = null
    private var sumPriceTV: TextView? = null
    private var spinnerCategoriesServices: Spinner? = null
    private var price: Long = 0
    private var listCategoriesServices: List<CategoryServices> = emptyList()
    private var branchAdminId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enroll, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOOLBAR)
        setMenu(R.menu.menu_save)
        showButtonBack()
        setListenerArrowBack()

        arguments?.let {
            listCars = it.get(LIST_CARS) as? List<CarModel>
        }

        val carSpinner: Spinner = view.findViewById(R.id.carSpinner)
        sumPriceTV = view.findViewById(R.id.sumPriceTextView)
        servicesListRecyclerViewAdapter =
            ServicesListRecyclerViewAdapter { sum -> setSumPriceService(sum) }
        branchSpinner = view.findViewById(R.id.branchSpinner)
        val serviceListRecyclerView: RecyclerView = view.findViewById(R.id.servicesListRecyclerView)
        spinnerCategoriesServices = view.findViewById(R.id.spinnerCategoriesServices)

        serviceListRecyclerView.adapter = servicesListRecyclerViewAdapter
        loadBranchList()

        // Заполняем спинер списком автомобилей
        listCars?.let {
            val carSpinnerAdapter: ArrayAdapter<CarModel> = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item, it
            )
            carSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            carSpinner.adapter = carSpinnerAdapter
        }

        branchSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val branch: BranchModel =
                    parent?.getItemAtPosition(position) as? BranchModel ?: BranchModel()

                branchAdminId = branch.adminId
                loadListCategoriesServices(this@EnrollFragment)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.e("SELECTED", "Ничего не выбранно")
            }
        }

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionOk -> {
                    // Поменять на callBack вместо метода RecyclerViewAdapter
                    val list: List<ServiceModel> =
                        servicesListRecyclerViewAdapter?.getCheckedServices() ?: emptyList()
                    if (list.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Выберити хотя бы одну услугу",
                            Toast.LENGTH_SHORT
                        ).show()
                        false
                    } else {
                        val chooseBranch: BranchModel =
                            branchSpinner?.selectedItem as? BranchModel ?: BranchModel()
                        val chooseCar = carSpinner.selectedItem as? CarModel ?: CarModel()

                        transferDataOfEnroll(list, chooseBranch, chooseCar.id)

                        true
                    }
                }
                R.id.actionExit -> {
                    signOut()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Загружает список категорий услуг
     * */

    private fun loadListCategoriesServices(callBack: EnrollFragment) {
        val weakReferenceCallBack = WeakReference(callBack)
        val childName = "categories"
        val adminId = branchAdminId

        val query = reference.child(childName).orderByChild(CHILD_NAME_ADMIN_ID).equalTo(adminId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listCategories: MutableList<CategoryServices> = mutableListOf()
                for (child in snapshot.children) {
                    val serviceModel = child.getValue<CategoryServices>()
                    serviceModel?.let {
                        listCategories.add(it)
                    }
                }
                weakReferenceCallBack.get()?.setListCategories(listCategories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Обновляем значение итоговой суммы
     * */

    private fun setSumPriceService(sum: Int) {
        val sumText = "Итого: " + sum.toString() + "руб."
        sumPriceTV?.text = sumText
        price = sum.toLong()
    }

    /**
     * Загружает список улуг по копаниям
     * */

    private fun loadListService(adminId: String) {
        val listServices: MutableList<ServiceModel> = mutableListOf()

        val branchQuery =
            reference.child(CHILD_SERVICES).orderByChild(ORDER_KEY_ADMIN_ID).equalTo(adminId)

        branchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val serviceModel = child.getValue<ServiceModel>()
                    serviceModel?.let { listServices.add(it) }
                    // Handler(Looper.getMainLooper()).post {
                    callBack.setListServices(listServices)
                    // }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Загружает список филиалов
     * */

    private fun loadBranchList() {
        val listBranch: MutableList<BranchModel> = mutableListOf()

        val branchQuery = reference.child(CHILD_BRANCH)

        branchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val branch = child.getValue<BranchModel>()
                    branch?.let { listBranch.add(it) }
                    /*Handler(Looper.getMainLooper()).post {

                    }*/

                    callBack.setListBranch(listBranch)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Переходим на фрагмент для генерации и выбора талонов на запись
     * */

    private fun transferDataOfEnroll(
        listService: List<ServiceModel>,
        branch: BranchModel,
        carID: String
    ) {
        setFragmentResult(
            MainActivity.SHOW_SELECT_DATE_TIME,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.LIST_SERVICES to listService,
                MainActivity.BRANCH to branch,
                MainActivity.CAR_ID to carID,
                MainActivity.PRICE to price
            )
        )
    }

    companion object {
        private const val CHILD_BRANCH = "branch"
        private const val CHILD_SERVICES = "services"
        private const val ORDER_KEY_ADMIN_ID = "adminId"
        private const val LIST_CARS = "list_cars"
        private const val CHILD_NAME_ADMIN_ID = "adminId"

        private const val TITLE_TOOLBAR = "Запись"

        fun newInstance(listCars: List<CarModel>): EnrollFragment {
            return EnrollFragment().apply {
                arguments = bundleOf(LIST_CARS to listCars)
            }
        }
    }

    override fun setListBranch(list: List<BranchModel>) {
        val branchSpinnerAdapter: ArrayAdapter<BranchModel> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, list
        )

        branchSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        branchSpinner?.adapter = branchSpinnerAdapter
    }

    override fun setListServices(listServices: List<ServiceModel>) {

        val spinnerAdapter: ArrayAdapter<CategoryServices> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, listCategoriesServices
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        spinnerCategoriesServices?.adapter = spinnerAdapter

        spinnerCategoriesServices?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val category = parent?.getItemAtPosition(position) as? CategoryServices
                        ?: CategoryServices()
                    val listServiceOrderByCategory = mutableListOf<ServiceModel>()
                    for (services in listServices) {
                        if (services.category == category.name) {
                            listServiceOrderByCategory.add(services)
                        }
                    }
                    servicesListRecyclerViewAdapter?.setDataSet(listServiceOrderByCategory)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Log.e("SELECTED", "Ничего не выбранно")
                }
            }
    }

    override fun setListCategories(list: List<CategoryServices>) {
        listCategoriesServices = list
        loadListService(branchAdminId)
    }
}
