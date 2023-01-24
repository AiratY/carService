package ru.airatyunusov.carservice.admin.services

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
import ru.airatyunusov.carservice.BaseFragment
import ru.airatyunusov.carservice.MainActivity
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.callbacks.ServicesCallBack
import ru.airatyunusov.carservice.model.CategoryServices
import ru.airatyunusov.carservice.model.ServiceModel
import java.lang.ref.WeakReference

class CatalogServicesFragment : BaseFragment(), ServicesCallBack {

    private var recyclerView: RecyclerView? = null
    private var serviceAdapter: ServicesRecyclerViewAdapter? = null

    private var titleService: TextView? = null
    private var addService: TextView? = null
    private var progressBar: ProgressBar? = null
    private var titleCategoriesServices: TextView? = null
    private var spinnerCategoriesServices: Spinner? = null
    private var addCategories: TextView? = null
    private var changeCategory: TextView? = null

    private var listCategoriesServices: List<CategoryServices> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catalog_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_SERVICES)
        setMenuWithExit()

        recyclerView = view.findViewById(R.id.listServicesRecyclerView)
        serviceAdapter = ServicesRecyclerViewAdapter { serviceModel -> changeService(serviceModel) }
        addService = view.findViewById(R.id.addServicesTextView)
        titleService = view.findViewById(R.id.titleListServicesTextView)
        progressBar = view.findViewById(R.id.catalogServicesProgressBar)
        recyclerView?.adapter = serviceAdapter
        titleCategoriesServices = view.findViewById(R.id.titleCategoryServiceTextView)
        spinnerCategoriesServices = view.findViewById(R.id.spinnerCategoriesServices)
        addCategories = view.findViewById(R.id.addCategories)
        changeCategory = view.findViewById(R.id.changeCategories)

        loadListCategoriesServices(this)

        addService?.setOnClickListener {
            openFragmentToAddServices()
        }

        addCategories?.setOnClickListener {
            openFragmentToAddCategories()
        }

        changeCategory?.setOnClickListener {
            val category = spinnerCategoriesServices?.selectedItem as? CategoryServices
            category?.let { categoryServices -> openFragmentToChangeCategories(categoryServices) }
        }
    }

    /**
     * Осуществляет переход на другой фрагмент для изменения данных услуги
     * */

    private fun changeService(serviceModel: ServiceModel) {
        setFragmentResult(
            MainActivity.SHOW_ADD_SERVICE,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.SERVICE to serviceModel
            )
        )
    }

    /**
     * Осуществляет переход на фрагмент добавления услуги
     * */

    private fun openFragmentToAddServices() {
        setFragmentResult(
            MainActivity.SHOW_ADD_SERVICE,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
            )
        )
    }

    /**
     * Осуществляет переход на фрагмент добавления категории услуги
     * */

    private fun openFragmentToAddCategories() {
        setFragmentResult(
            MainActivity.SHOW_ADD_CATEGORY_SERVICE,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
            )
        )
    }

    /**
     * Осуществляет переход на фрагмент редактирования категории услуги
     * */

    private fun openFragmentToChangeCategories(category: CategoryServices) {
        setFragmentResult(
            MainActivity.SHOW_ADD_CATEGORY_SERVICE,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.CATEGORY to category
            )
        )
    }

    /**
     * Загружает список оказываемых услуг
     * */

    private fun loadListServices(callBack: ServicesCallBack) {
        val weakReferenceCallBack = WeakReference(callBack)
        val childName = getString(R.string.services_firebase_key)
        val adminId = getUserId()

        val query = reference.child(childName).orderByChild(CHILD_NAME_ADMIN_ID).equalTo(adminId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listServices: MutableList<ServiceModel> = mutableListOf()
                for (child in snapshot.children) {
                    val serviceModel = child.getValue<ServiceModel>()
                    serviceModel?.let {
                        listServices.add(it)
                    }
                }
                weakReferenceCallBack.get()?.setListServices(listServices)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Загружает список оказываемых услуг
     * */

    private fun loadListCategoriesServices(callBack: ServicesCallBack) {
        val weakReferenceCallBack = WeakReference(callBack)
        val childName = "categories"
        val adminId = getUserId()

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
                weakReferenceCallBack.get()
                    ?.setListCategoriesServices(listCategories, weakReferenceCallBack)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Скрывает ProgressBar
     * */

    private fun goneProgressBar() {
        progressBar?.visibility = View.GONE
    }

    companion object {
        private const val TITLE_SERVICES = "Услуги"
        private const val CHILD_NAME_ADMIN_ID = "adminId"
    }

    /**
     * Обновляет список услуг
     * */

    override fun setListServices(listServices: List<ServiceModel>) {
        goneProgressBar()
        visibleAllViews()

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
                    serviceAdapter?.setDateSet(listServiceOrderByCategory)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Log.e("SELECTED", "Ничего не выбранно")
                }
            }
    }

    override fun setListCategoriesServices(
        listCategory: List<CategoryServices>,
        weakReference: WeakReference<ServicesCallBack>
    ) {
        if (listCategory.isEmpty()) {
            showMessage()
        } else {
            listCategoriesServices = listCategory
            weakReference.get()?.let { loadListServices(it) }
        }
    }

    private fun showMessage() {
        Toast.makeText(requireContext(), "Добавьте категорию услуг", Toast.LENGTH_SHORT).show()
    }

    override fun isShowBottomNavigationView(): Boolean {
        return true
    }

    /**
     * показывает все виджеты
     * */

    private fun visibleAllViews() {
        titleService?.visibility = View.VISIBLE
        addService?.visibility = View.VISIBLE
    }
}
