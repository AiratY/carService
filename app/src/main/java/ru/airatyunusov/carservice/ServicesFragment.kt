package ru.airatyunusov.carservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.os.bundleOf
import ru.airatyunusov.carservice.model.ServiceModel

class ServicesFragment : BaseFragment() {

    private var nameEditText: EditText? = null
    private var priceEditText: EditText? = null
    private var hoursNumberPicker: NumberPicker? = null

    private var services: ServiceModel? = null

    private var name = ""
    private var hours = 0
    private var price = 0

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

        setTitle("Услуга")
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
            val service = ServiceModel(key, getUserId(), name, hours, price)
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
        hours = if (hoursNumberPicker?.value.toString().isEmpty()) 0 else hoursNumberPicker?.value.toString().toInt()
        price = if (priceEditText?.text.toString().isEmpty()) 0 else priceEditText?.text.toString().toInt()
    }

    /**
     * Осуществляет валидацию полей
     * */

    private fun checkValueEditTexts(): Boolean {
        return if (name.isEmpty() || (hours == 0) || price == 0) {
            Toast.makeText(requireContext(), "Поля не должны быть пустыми", Toast.LENGTH_LONG)
                .show()
            false
        } else {
            true
        }
    }

    companion object {
        private const val SERVICE = "service"

        fun newInstance(service: ServiceModel): ServicesFragment {
            return ServicesFragment().apply {
                arguments = bundleOf(SERVICE to service)
            }
        }
    }
}
