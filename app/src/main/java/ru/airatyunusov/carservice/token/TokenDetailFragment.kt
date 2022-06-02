package ru.airatyunusov.carservice.token

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.BaseFragment
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.callbacks.TokenCallBack
import ru.airatyunusov.carservice.model.*
import java.lang.ref.WeakReference

class TokenDetailFragment : BaseFragment(), TokenCallBack {

    private var carId = ""
    private var branchId = ""
    private var employeeId = ""
    private var tokenId = ""
    private var isDelete = true
    private var callBack: WeakReference<TokenCallBack>? = null

    private var progressBar: ProgressBar? = null
    private var deleteTokenButton: Button? = null

    private var branchTextView: TextView? = null
    private var employeeTextView: TextView? = null
    private var carTextView: TextView? = null
    private var servicesTextView: TextView? = null
    private var priceTextView: TextView? = null
    private var dateTimeTextView: TextView? = null

    private var titleBranchTextView: TextView? = null
    private var titleEmployeeTextView: TextView? = null
    private var titleCarTextView: TextView? = null
    private var titleServicesTextView: TextView? = null
    private var titlePriceTextView: TextView? = null
    private var titleDateTimeTextView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_token_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOOLBAR)
        showButtonBack()
        showButtonBack()
        setListenerArrowBack()
        setMenuWithExit()

        callBack = WeakReference(this)
        dateTimeTextView = view.findViewById(R.id.startDateTimeTextView)
        branchTextView = view.findViewById(R.id.branchTextView)
        employeeTextView = view.findViewById(R.id.employeeTextView)
        carTextView = view.findViewById(R.id.carTextView)
        servicesTextView = view.findViewById(R.id.servicesTextView)
        deleteTokenButton = view.findViewById(R.id.deleteTokenButton)
        priceTextView = view.findViewById(R.id.priceTextView)
        progressBar = view.findViewById(R.id.descTokenProgressBar)

        titleBranchTextView = view.findViewById(R.id.titleBranchTextView)
        titleEmployeeTextView = view.findViewById(R.id.titleEmployeeTextView)
        titleCarTextView = view.findViewById(R.id.titleCarTextView)
        titleServicesTextView = view.findViewById(R.id.titleServicesTextView)
        titlePriceTextView = view.findViewById(R.id.titlePriceTextView)
        titleDateTimeTextView = view.findViewById(R.id.titleStartDateTimeTextView)

        arguments?.let {
            isDelete = it.getBoolean(IS_DELETE, true)

            val token = it.get(TOKEN) as? TokenFirebaseModel ?: TokenFirebaseModel()
            dateTimeTextView?.text = token.toString()
            val priceText = "${token.price} руб."
            priceTextView?.text = priceText
            for (services in token.listServices) {
                servicesTextView?.append(services.name + ",\n")
            }
            branchId = token.branchId
            carId = token.carId
            employeeId = token.idEmployee
            tokenId = token.id
            loadBranch()
            loadCar()
            loadEmployee()
        }

        deleteTokenButton?.setOnClickListener {
            removeToken()
            returnBack()
        }
    }

    private fun removeToken() {
        reference.child(TICKET_FIREBASE_KEY).child(tokenId).removeValue()
    }

    /**
     * Загружает данные о сотруднике оказывающую данную услугу из БД
     * */

    private fun loadEmployee() {
        val childName = getString(R.string.employees_firebase_key)

        val query = reference.child(childName).orderByChild("id").equalTo(employeeId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val employee = child.getValue<Employee>()
                    employee?.let { callBack?.get()?.setEmployee(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Загружает данные об автомобле из БД
     * */

    private fun loadCar() {
        val query = reference.child(CARS).orderByChild("id").equalTo(carId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val car = child.getValue<CarModel>()
                    car?.let { callBack?.get()?.setCar(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Загружает данные об филиале,в котором будут делать услугу, из БД
     * */

    private fun loadBranch() {
        val childName = getString(R.string.branch_firebase_key)

        val branchQuery = reference.child(childName).orderByChild("id").equalTo(branchId)

        branchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val branch = child.getValue<BranchModel>()
                    branch?.let { callBack?.get()?.setBranch(it) }
                }
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
        private const val TITLE_TOOLBAR = "Описание записи"
        private const val TOKEN = "TOKEN"
        private const val CARS = "cars"
        private const val TICKET_FIREBASE_KEY = "tickets"
        private const val IS_DELETE = "is_delete"
        fun newInstance(token: TokenFirebaseModel, isDelete: Boolean): TokenDetailFragment {
            return TokenDetailFragment().apply {
                arguments = bundleOf(TOKEN to token, IS_DELETE to isDelete)
            }
        }
    }

    /**
     * Отображает данные об автомобиле
     * */

    override fun setCar(car: CarModel) {
        carTextView?.text = car.toString()
        visibleAllTextView()
        goneProgressBar()
    }

    /**
     * Отображает данные о сотруднике
     * */

    override fun setEmployee(employee: Employee) {
        employeeTextView?.text = employee.toString()
    }

    /**
     * Показывает все TextView
     * */

    private fun visibleAllTextView() {
        titleBranchTextView?.visibility = View.VISIBLE
        titleEmployeeTextView?.visibility = View.VISIBLE
        titleCarTextView?.visibility = View.VISIBLE
        titleServicesTextView?.visibility = View.VISIBLE
        titlePriceTextView?.visibility = View.VISIBLE
        titleDateTimeTextView?.visibility = View.VISIBLE

        branchTextView?.visibility = View.VISIBLE
        employeeTextView?.visibility = View.VISIBLE
        carTextView?.visibility = View.VISIBLE
        servicesTextView?.visibility = View.VISIBLE
        priceTextView?.visibility = View.VISIBLE
        dateTimeTextView?.visibility = View.VISIBLE

        if (isDelete) {
            deleteTokenButton?.visibility = View.VISIBLE
        }
    }

    /**
     * Отображает данные о филиале
     * */

    override fun setBranch(branch: BranchModel) {
        branchTextView?.text = branch.toString()
    }
}
