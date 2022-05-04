package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.callbacks.CustomerCallBack
import ru.airatyunusov.carservice.model.CarModel
import ru.airatyunusov.carservice.model.FirebaseHelper
import ru.airatyunusov.carservice.model.TokenFirebaseModel

class CustomerFragment : Fragment(), CustomerCallBack {

    private var carsRecyclerView: RecyclerView? = null
    private var carAdapterRecyclerView: CarRecyclerViewAdapter? = null
    private var tokenRecyclerViewAdapter: TokenRecyclerViewAdapter? = null
    private var addButton: Button? = null

    private var isVisibleMyCars = false

    private val reference = FirebaseHelper().getDatabaseReference()

    private var listCars: List<CarModel>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val myServiceTV: TextView = view.findViewById(R.id.myServicesTextView)
        val myCarsTv: TextView = view.findViewById(R.id.myCarsTextView)
        carsRecyclerView = view.findViewById(R.id.listCarsRecyclerView)
        val tokenRecyclerView: RecyclerView = view.findViewById(R.id.listCustomersTokenRecyclerView)
        tokenRecyclerViewAdapter = TokenRecyclerViewAdapter { token -> openDetailToken(token) }
        addButton = view.findViewById(R.id.addCarButton)
        val enrollBtn: Button = view.findViewById(R.id.enrollNewButton)
        carAdapterRecyclerView = CarRecyclerViewAdapter { carModel -> editCarModel(carModel) }
        carsRecyclerView?.adapter = carAdapterRecyclerView

        tokenRecyclerView.adapter = tokenRecyclerViewAdapter

        goneListMyCars()

        loadListCustomerServices(this)
        loadListMyCars(this)

        myServiceTV.setOnClickListener {

        }

        myCarsTv.setOnClickListener {
            isVisibleMyCars = if (isVisibleMyCars) {
                goneListMyCars()
                false
            } else {
                visibleListMyCars()
                true
            }
        }

        addButton?.setOnClickListener {
            addCarModel()
        }

        enrollBtn.setOnClickListener {
            openEnrollPage()
        }
    }

    /**
     * Переходим на фрагмент с дктальным описанием записи
     * */

    private fun openDetailToken(token: TokenFirebaseModel) {
        setFragmentResult(
            MainActivity.SHOW_DETAIL_TOKEN,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.TOKEN to token
            )
        )
    }

    /**
     * Загружает список записей клиента
     * */

    private fun loadListCustomerServices(callback: CustomerCallBack) {
        val listToken: MutableList<TokenFirebaseModel> = mutableListOf()

        val tokenQuery =
            reference.child(TOKEN_MODEL_FIREBASE_KEY).orderByChild("userId").equalTo(getUserId())

        tokenQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val tokenFirebase = data.getValue<TokenFirebaseModel>() ?: TokenFirebaseModel()
                    listToken.add(tokenFirebase)
                }
                callback.setListToken(listToken)
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
        val userId = getUserId()
        val query = reference.child(CARS).orderByChild("userId").equalTo(userId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listCars: MutableList<CarModel> = mutableListOf()
                for (child in snapshot.children) {
                    val car = child.getValue<CarModel>()
                    car?.let { listCars.add(it) }
                }
                callBack.setListCars(listCars)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }


    /**
     * Показывает список моих автомобилей
     * */

    private fun visibleListMyCars() {
        carsRecyclerView?.visibility = View.VISIBLE
        addButton?.visibility = View.VISIBLE
    }

    /**
     * Скрывает список моих автомобилей
     * */

    private fun goneListMyCars() {
        carsRecyclerView?.visibility = View.GONE
        addButton?.visibility = View.GONE
    }

    companion object {
        const val CARS = "cars"
        private const val TOKEN_MODEL_FIREBASE_KEY = "tickets"

        /**
         * Возвращает Id клиента
         * нужно сделать by lazy
         * */

        fun getUserId(): String {
            return ""
        }
    }

    /**
     * Отображаем полученные значения
     * */

    override fun setListCars(listCars: List<CarModel>) {
        carAdapterRecyclerView?.setDateSet(listCars)
        this.listCars = listCars
    }

    override fun setListToken(listToken: List<TokenFirebaseModel>) {
        tokenRecyclerViewAdapter?.setDateSet(listToken)
    }
}