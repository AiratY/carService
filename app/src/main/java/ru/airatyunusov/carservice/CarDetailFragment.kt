package ru.airatyunusov.carservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import ru.airatyunusov.carservice.model.CarModel

class CarDetailFragment : BaseFragment() {

    private var makeCarEditText: EditText? = null
    private var modelCarEditText: EditText? = null
    private var numberCarEditText: EditText? = null
    private var yearCarEditText: EditText? = null
    private var regionCarEditText: EditText? = null

    private var make: String = ""
    private var model: String = ""
    private var number: String = ""
    private var year: Int = 0
    private var region: String = ""

    private var carModel: CarModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_car_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOOLBAR)
        showButtonBack()
        setListenerArrowBack()

        makeCarEditText = view.findViewById(R.id.makeCarEditText)
        modelCarEditText = view.findViewById(R.id.modelCarEditText)
        numberCarEditText = view.findViewById(R.id.numberCarEditText)
        yearCarEditText = view.findViewById(R.id.yearCarEditText)
        regionCarEditText = view.findViewById(R.id.numberRegionEditText)

        arguments?.let { arguments ->
            carModel = arguments.get(CAR) as? CarModel

            carModel?.let { car ->
                fillEditTexts(car)
            }
        }

        if (carModel == null) {
            setMenu(R.menu.menu_save)
        } else {
            setMenu(R.menu.menu_save_delete)
        }

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionExit -> {
                    signOut()
                    true
                }
                R.id.actionDelete -> {
                    removeCarModel()
                    returnBack()
                    true
                }
                R.id.actionOk -> {
                    if (checkValuesEditTexts()) {
                        val fullNumber = "$number $region"

                        val car = CarModel(
                            userId = getUserId(),
                            make = make,
                            model = model,
                            numberCar = fullNumber,
                            year = year
                        )
                        carModel?.let {
                            car.id = it.id
                        }
                        saveCar(car)
                        returnBack()
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
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
     * Заполняет все поля значениями автомобиля
     * */

    private fun fillEditTexts(car: CarModel) {
        makeCarEditText?.setText(car.make)
        modelCarEditText?.setText(car.model)

        val arr = car.numberCar.split(" ")
        numberCarEditText?.setText(arr[0])
        regionCarEditText?.setText(arr[1])
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
        getValuesEditText() // Обновляем значения

        if (make.isEmpty() || model.isEmpty() || number.isEmpty() || year == 0 || region.isEmpty()) {
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
        region = regionCarEditText?.text.toString()
    }

    companion object {
        private const val CAR = "CAR"
        private const val CHILD_CARS = "cars"
        private const val TITLE_TOOLBAR = "Автомобиль"

        fun newInstance(carModel: CarModel): CarDetailFragment {
            return CarDetailFragment().apply {
                arguments = bundleOf(CAR to carModel)
            }
        }
    }
}
