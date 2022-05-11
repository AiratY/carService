package ru.airatyunusov.carservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import ru.airatyunusov.carservice.model.ServiceModel

class ServicesFragment : BaseFragment() {

    private var nameEditText: EditText? = null
    private var hoursEditText: EditText? = null
    private var priceEditText: EditText? = null
    private var deleteBtn: Button? = null

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
        childName = getString(R.string.services_firebase_key)
        nameEditText = view.findViewById(R.id.nameServiceEditText)
        hoursEditText = view.findViewById(R.id.countHoursServiceEditText)
        priceEditText = view.findViewById(R.id.priceServiceEditText)
        val saveBtn: Button = view.findViewById(R.id.saveServiceButton)
        deleteBtn = view.findViewById(R.id.deleteServicesButton)

        arguments?.let {
            services = it.get(SERVICE) as? ServiceModel
        }

        services?.let {
            visibleDeleteBtn()
            setEditText(it)
        }

        deleteBtn?.setOnClickListener {
            removeService()
            returnBack()
        }

        saveBtn.setOnClickListener {
            updateValueEditText()
            if (checkValueEditTexts()) {
                saveService()
                returnBack()
            }
        }
    }

    /**
     * Заполняет поля существующим значением услуги
     * */

    private fun setEditText(serviceModel: ServiceModel) {
        nameEditText?.setText(serviceModel.name)
        hoursEditText?.setText(serviceModel.hours.toString())
        priceEditText?.setText(serviceModel.price.toString())
    }

    private fun visibleDeleteBtn() {
        deleteBtn?.visibility = View.VISIBLE
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
        hours = if (hoursEditText?.text.toString().isEmpty()) 0 else hoursEditText?.text.toString().toInt()
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
