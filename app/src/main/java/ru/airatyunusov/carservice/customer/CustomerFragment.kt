package ru.airatyunusov.carservice.customer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import ru.airatyunusov.carservice.callbacks.CustomerCallBack
import ru.airatyunusov.carservice.customer.cars.CarRecyclerViewAdapter
import ru.airatyunusov.carservice.model.CarModel
import ru.airatyunusov.carservice.model.User
import java.lang.ref.WeakReference

class CustomerFragment : BaseFragment(), CustomerCallBack {

    private var carsRecyclerView: RecyclerView? = null
    private var carAdapterRecyclerView: CarRecyclerViewAdapter? = null
    private var addTextView: TextView? = null
    private var phoneTextView: TextView? = null

    private var listCars: List<CarModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOOLBAR)
        setMenuWithExit()

        val myCarsTv: TextView = view.findViewById(R.id.titleCarsTextView)
        carsRecyclerView = view.findViewById(R.id.listCarsRecyclerView)

        addTextView = view.findViewById(R.id.addNewCarTextView)
        carAdapterRecyclerView = CarRecyclerViewAdapter { carModel -> editCarModel(carModel) }
        carsRecyclerView?.adapter = carAdapterRecyclerView
        phoneTextView = view.findViewById(R.id.phoneTextView)

        val nameTextView: TextView = view.findViewById(R.id.nameCustomerTextView)
        nameTextView.text = getNameUser()

        loadListMyCars(this)
        loadDataUser(this)

        view.findViewById<Button>(R.id.enrollButton).setOnClickListener {
            if (listCars?.isEmpty() == true) {
                Toast.makeText(requireContext(), "Нужно добавить автомобиль", Toast.LENGTH_SHORT)
                    .show()
            } else {
                openEnrollPage()
            }
        }

        addTextView?.setOnClickListener {
            addCarModel()
        }
    }

    private fun loadDataUser(callback: CustomerCallBack) {
        val weakReference = WeakReference(callback)
        val userId = getUserId()
        val query = reference.child("users").orderByChild("id").equalTo(userId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue<User>()
                    user?.let { weakReference.get()?.setUser(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Переходим на фрагмент для записиси
     * */

    private fun openEnrollPage() {
        setFragmentResult(
            MainActivity.SHOW_ENROLL,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.LIST_CARS to listCars
            )
        )
    }

    /**
     * Переходит на другой фрагмент для добавления автомобиля
     * */

    private fun addCarModel() {
        setFragmentResult(
            MainActivity.SHOW_DETAIL_CAR,
            bundleOf(
                MainActivity.BUNDLE_KEY to true
            )
        )
    }

    /**
     * Передает данные об автомобиле в другой фрагмент для из изменения
     * */

    private fun editCarModel(carModel: CarModel) {
        setFragmentResult(
            MainActivity.SHOW_DETAIL_CAR,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.CAR to carModel
            )
        )
    }

    /**
     * Загружает список автомобилей из БД
     * */

    private fun loadListMyCars(callBack: CustomerCallBack) {
        val weakReference = WeakReference(callBack)
        val userId = getUserId()
        val query = reference.child(CARS).orderByChild("userId").equalTo(userId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listCars: MutableList<CarModel> = mutableListOf()
                for (child in snapshot.children) {
                    val car = child.getValue<CarModel>()
                    car?.let { listCars.add(it) }
                }
                weakReference.get()?.setListCars(listCars)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    companion object {
        const val CARS = "cars"
        private const val TITLE_TOOLBAR = "Профиль"
    }

    /**
     * Отображаем полученные значения
     * */

    override fun setListCars(listCars: List<CarModel>) {
        carAdapterRecyclerView?.setDateSet(listCars)
        this.listCars = listCars
    }

    override fun setUser(user: User) {
        phoneTextView?.text = user.phone.toString()
    }

    override fun isShowBottomNavigationView(): Boolean {
        return true
    }
}
