package ru.airatyunusov.carservice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import ru.airatyunusov.carservice.model.CarModel
import ru.airatyunusov.carservice.model.FirebaseHelper

class CarDetailFragment : Fragment() {

    private var makeCarEditText: EditText? = null
    private var modelCarEditText: EditText? = null
    private var numberCarEditText: EditText? = null
    private var yearCarEditText: EditText? = null
    private var saveButton: Button? = null
    private var deleteButton: Button? = null

    private var make: String = ""
    private var model: String = ""
    private var number: String = ""
    private var year: Int = 0

    private var carModel: CarModel? = null

    private val reference = FirebaseHelper().getDatabaseReference()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_car_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        makeCarEditText = view.findViewById(R.id.makeCarEditText)
        modelCarEditText = view.findViewById(R.id.modelCarEditText)
        numberCarEditText = view.findViewById(R.id.numberCarEditText)
        yearCarEditText = view.findViewById(R.id.yearCarEditText)
        saveButton = view.findViewById(R.id.saveCarButton)
        deleteButton = view.findViewById(R.id.deleteCarButton)

        goneDeleteBtn()

        arguments?.let { arguments ->
            carModel = arguments.get(CAR) as? CarModel

            carModel?.let { car ->
                fillEditTexts(car)
                visibleDeleteBtn()
            }
        }

        saveButton?.setOnClickListener {

            if (checkValuesEditTexts()) {
                val car = CarModel(
                    userId = CustomerFragment.getUserId(),
                    make = make,
                    model = model,
                    numberCar = number,
                    year = year
                )
                carModel?.let {
                    car.id = it.id
                }
                saveCar(car)
                returnBack()
            }
        }

        deleteButton?.setOnClickListener {
            removeCarModel()
            returnBack()
        }
    }

    /**
     * Отображает кнопку удаления автомобиля
     * */

    private fun visibleDeleteBtn() {
        deleteButton?.visibility = View.VISIBLE
    }

    /**
     * Скрывает кнопку удаления автомобиля
     * */

    private fun goneDeleteBtn() {
        deleteButton?.visibility = View.GONE
    }

    /**
     * Удаляет из БД автомобиль
     * */

    private fun removeCarModel() {
        carModel?.let {
            reference.child(CHILD_CARS).child(it.id).removeValue()
        }
    }
    /**
     * Возвращает назад
     * */

    private fun returnBack() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    /**
     * Заполняет все поля значениями автомобиля
     * */

    private fun fillEditTexts(car: CarModel) {
        makeCarEditText?.setText(car.make)
        modelCarEditText?.setText(car.model)
        numberCarEditText?.setText(car.numberCar)
        yearCarEditText?.setText(car.year.toString())
    }

    /**
     * Сохраняет автомобиль клиента в БД
     * */

    private fun saveCar(carModel: CarModel) {
        val key: String
        if (carModel.id == "") {
            key = reference.push().key ?: ""
            carModel.id = key
        } else {
            key = carModel.id
        }
        val childUpdates = hashMapOf<String, Any>(
            "/${CustomerFragment.CARS}/$key" to carModel
        )
        reference.updateChildren(childUpdates)
    }

    /**
     * Осуществлет валидацию полей
     * */

    private fun checkValuesEditTexts(): Boolean {
        getValuesEditText()//Обновляем значения

        if (make.isEmpty() || model.isEmpty() || number.isEmpty() || year == 0) {
            Toast.makeText(
                requireContext(),
                "Поля не должны быть пустыми",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (number.length < 6) {
            Toast.makeText(
                requireContext(),
                "Номер состоит из 6 символов",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (year / 1000 < 1) {
            Toast.makeText(
                requireContext(),
                "Год должен состоять из 4 цифр",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else {
            return true
        }
    }


    /**
     * Записывает текузие значение в переменные
     * */

    private fun getValuesEditText() {
        make = makeCarEditText?.text.toString()
        model = modelCarEditText?.text.toString()
        number = numberCarEditText?.text.toString()
        year = if (yearCarEditText?.text.toString().isEmpty()) {
            0
        } else {
            yearCarEditText?.text.toString().toInt()
        }
    }

    companion object {
        private const val CAR = "CAR"
        private const val CHILD_CARS = "cars"

        fun newInstance(carModel: CarModel): CarDetailFragment {
            return CarDetailFragment().apply {
                arguments = bundleOf(CAR to carModel)
            }
        }
    }
}