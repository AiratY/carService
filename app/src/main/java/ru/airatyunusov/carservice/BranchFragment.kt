package ru.airatyunusov.carservice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import ru.airatyunusov.carservice.callbacks.BranchCallBack
import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.Employee
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class BranchFragment : Fragment(), BranchCallBack {
    private var listEmployeeRecyclerView: RecyclerView? = null
    private val database =
        Firebase.database("https://carservice-93ef9-default-rtdb.europe-west1.firebasedatabase.app/")
    private var branchId = ""
    private var branch: BranchModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_branch, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listEmployeeRecyclerView = view.findViewById(R.id.listEmployeeRecyclerView)
        val addEmployee: Button = view.findViewById(R.id.addEmployeeButton)
        val addressTextView = view.findViewById<TextView>(R.id.addressBranchTextView)
        val phoneTextView: TextView = view.findViewById(R.id.phoneBranchTextView)
        val editBtn: Button = view.findViewById(R.id.editButton)

        arguments?.let {
            branch = it.get(BRANCH) as? BranchModel

            branch?.let { it ->
                branchId = it.id
                addressTextView.text = it.address
                phoneTextView.text = "Тел: ${it.phone}"
            }

            val executor = Executors.newSingleThreadExecutor()
            val callBack: WeakReference<BranchCallBack> = WeakReference(this)

            executor.execute {
                val listEmployee = loadListEmployee()
                Handler(Looper.getMainLooper()).post {
                    callBack.get()?.setListEmployee(listEmployee)
                }
            }
        }

        addEmployee.setOnClickListener {
            setFragmentResult(
                MainActivity.SHOW_EMPLOYEE,
                bundleOf(MainActivity.BUNDLE_KEY to true, MainActivity.BRANCH_ID to branchId)
            )
        }

        editBtn.setOnClickListener {
            setFragmentResult(
                MainActivity.SHOW_DETAIL_BRANCH,
                bundleOf(MainActivity.BUNDLE_KEY to true, MainActivity.BRANCH to branch)
            )
        }
    }

    private fun loadListEmployee(): List<Employee> {
        val childName = getString(R.string.employees_firebase_key)

        val lisEmployee: MutableList<Employee> = mutableListOf()

        var allCount = 0L
        var count = 1L

        val query =
            database.reference.child(childName).orderByChild("branchId").equalTo(branchId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (child in snapshot.children) {
                    val employee = child.getValue<Employee>()
                    employee?.let { lisEmployee.add(it) }
                    count++
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(FIREBASE_LOG_TAG, error.message)
            }
        })

        val dataSnapshot = query.get()
        while (count < allCount || !dataSnapshot.isComplete) {
            Thread.sleep(500)
        }

        return lisEmployee
    }

    private fun editEmployee(employee: Employee) {
        setFragmentResult(
            MainActivity.SHOW_EMPLOYEE,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.BRANCH_ID to branchId,
                MainActivity.EMPLOYEE to employee
            )
        )
    }

    companion object {
        private const val FIREBASE_LOG_TAG = "Firebase"
        private const val EMPLOYEES_FIREBASE_KEY = "employees"
        private const val BRANCH_ID = "branch_id"
        private const val BRANCH = "branch"

        fun newInstance(branch: BranchModel): BranchFragment {
            return BranchFragment().apply {
                arguments = bundleOf(BRANCH to branch)
            }
        }

        fun newInstance(branchID: String): BranchFragment {
            return BranchFragment().apply {
                arguments = bundleOf(BRANCH_ID to branchID)
            }
        }
    }

    override fun setListEmployee(listEmployee: List<Employee>) {
        val employeeAdapter = EmployeeRecyclerViewAdapter { employee -> editEmployee(employee) }
        employeeAdapter.setDateSet(listEmployee)
        listEmployeeRecyclerView?.adapter = employeeAdapter
    }
}
