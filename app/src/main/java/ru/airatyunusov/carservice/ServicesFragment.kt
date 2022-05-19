package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.callbacks.CategoryCallBack
import ru.airatyunusov.carservice.model.CategoryServices
import ru.airatyunusov.carservice.model.ServiceModel
import java.lang.ref.WeakReference

class ServicesFragment : BaseFragment(), CategoryCallBack {

    private var nameEditText: EditText? = null
    private var priceEditText: EditText? = null
    private var hoursNumberPicker: NumberPicker? = null
    private var spinnerCategoriesServices: Spinner? = null
    private var services: ServiceModel? = null

    private var name = ""
    private var hours = 0
    private var price = 0
    private var category = ""

    private var childName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE)
        showButtonBack()
        setListenerArrowBack()

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionExit -> {
                    signOut()
                    true
                }
                R.id.actionDelete -> {
                    removeService()
                    returnBack()
                    true
                }
                R.id.actionOk -> {
                    updateValueEditText()
                    if (checkValueEditTexts()) {
                        saveService()
                        returnBack()
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }

        childName = getString(R.string.services_firebase_key)
        nameEditText = view.findViewById(R.id.nameServiceEditText)
        priceEditText = view.findViewById(R.id.priceServiceEditText)
        hoursNumberPicker = view.findViewById(R.id.hoursServiceNumberPicker)
        spinnerCategoriesServices = view.findViewById(R.id.spinnerCategoriesServices)

        hoursNumberPicker?.minValue = 1
        hoursNumberPicker?.maxValue = 24

        arguments?.let {
            services = it.get(SERVICE) as? ServiceModel
        }

        services?.let {
            setEditText(it)
        }

        if (services == null) {
            setMenu(R.menu.menu_save)
        } else {
            setMenu(R.menu.menu_save_delete)
        }

        loadListCategoriesServices(this)
    }
    /**
     * Загружает список категорий услуг
     * */

    private fun loadListCategoriesServices(callBack: CategoryCallBack) {
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
                weakReferenceCallBack.get()?.setListCategories(listCategories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Заполняет поля существующим значением услуги
     * */

    private fun setEditText(serviceModel: ServiceModel) {
        nameEditText?.setText(serviceModel.name)
        priceEditText?.setText(serviceModel.price.toString())
        hoursNumberPicker?.value = serviceModel.hours
    }

    /**
     * Удаляет услугу из БД
     * */

    private fun removeService() {
        services?.let {
            reference.child(childName).child(it.id).removeValue()
        }
    }

    /**
     * Сохраняет услугу в БД
     * */

    private fun saveService() {
        val key = if (services == null) {
            reference.child(childName).push().key
        } else {
            services?.id
        }
        key?.let {
            val service = ServiceModel(key, getUserId(), name, hours, price, category)
            val childUpdates = hashMapOf<String, Any>(
                "/$childName/$key" to service
            )
            reference.updateChildren(childUpdates)
        }
    }

    /**
     * Обновляем переменные значениямя в EditTexts
     * */

    private fun updateValueEditText() {
        name = nameEditText?.text.toString()
        hours = if (hoursNumberPicker?.value.toString()
            .isEmpty()
        ) 0 else hoursNumberPicker?.value.toString().toInt()
        price = if (priceEditText?.text.toString().isEmpty()) 0 else priceEditText?.text.toString()
            .toInt()
        category = spinnerCategoriesServices?.selectedItem.toString()
    }

    /**
     * Осуществляет валидацию полей
     * */

    private fun checkValueEditTexts(): Boolean {
        return if (name.isEmpty() || (hours == 0) || price == 0 || category.isEmpty()) {
            Toast.makeText(requireContext(), "Поля не должны быть пустыми", Toast.LENGTH_LONG)
                .show()
            false
        } else {
            true
        }
    }

    companion object {
        private const val SERVICE = "service"
        private const val CHILD_NAME_ADMIN_ID = "adminId"
        private const val TITLE = "Услуга"

        fun newInstance(service: ServiceModel): ServicesFragment {
            return ServicesFragment().apply {
                arguments = bundleOf(SERVICE to service)
            }
        }
    }

    override fun setListCategories(list: List<CategoryServices>) {
        val spinnerAdapter: ArrayAdapter<CategoryServices> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, list
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinnerCategoriesServices?.adapter = spinnerAdapter

        services?.let {
            var i = 0
            for (category in list) {
                if (category.name == it.category) {
                    spinnerCategoriesServices?.setSelection(i)
                    break
                }
                i++
            }
        }
    }
}
