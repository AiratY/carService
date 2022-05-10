package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.callbacks.TokenCallBack
import ru.airatyunusov.carservice.model.*
import java.lang.ref.WeakReference

class TokenDetailFragment : BlankFragment(), TokenCallBack {

    private var carId = ""
    private var branchId = ""
    private var employeeId = ""
    private var tokenId = ""
    private var callBack: WeakReference<TokenCallBack>? = null

    private var branchTextView: TextView? = null
    private var employeeTextView: TextView? = null
    private var carTextView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_token_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        callBack = WeakReference(this)
        val dateTimeTextView: TextView = view.findViewById(R.id.dateTimeTextView)
        branchTextView = view.findViewById(R.id.branchTextView)
        employeeTextView = view.findViewById(R.id.employeeTextView)
        carTextView = view.findViewById(R.id.carTextView)
        val servicesTextView: TextView = view.findViewById(R.id.servicesTextView)
        val deleteTokenButton: Button = view.findViewById(R.id.deleteTokenButton)
        val priceTextView: TextView = view.findViewById(R.id.priceTextView)

        arguments?.let {
            val isDelete = it.getBoolean(IS_DELETE, true)
            if (!isDelete) {
                deleteTokenButton.visibility = View.GONE
            }
            val token = it.get(TOKEN) as? TokenFirebaseModel ?: TokenFirebaseModel()
            dateTimeTextView.text = token.toString()
            priceTextView.text = "${token.price} руб."
            for (services in token.listServices) {
                servicesTextView.append(services.name + ",\n")
            }
            branchId = token.branchId
            carId = token.carId
            employeeId = token.idEmployee
            tokenId = token.id
            loadBranch()
            loadCar()
            loadEmployee()
        }

        deleteTokenButton.setOnClickListener {
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

    companion object {
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
    }

    /**
     * Отображает данные о сотруднике
     * */

    override fun setEmployee(employee: Employee) {
        employeeTextView?.text = employee.toString()
    }

    /**
     * Отображает данные о филиале
     * */

    override fun setBranch(branch: BranchModel) {
        branchTextView?.text = branch.toString()
    }
}
