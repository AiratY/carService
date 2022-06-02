package ru.airatyunusov.carservice.admin.branchs

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.BaseFragment
import ru.airatyunusov.carservice.admin.employees.EmployeeRecyclerViewAdapter
import ru.airatyunusov.carservice.MainActivity
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.callbacks.BranchCallBack
import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.Employee
import java.lang.ref.WeakReference

class BranchFragment : BaseFragment(), BranchCallBack {
    private var listEmployeeRecyclerView: RecyclerView? = null
    private var branchId = ""
    private var branch: BranchModel? = null
    private var employeeAdapter: EmployeeRecyclerViewAdapter? = null

    private var iconImageView: ImageView? = null
    private var addressTextView: TextView? = null
    private var phoneTextView: TextView? = null
    private var titleEmployeeTextView: TextView? = null
    private var addEmployeeTextView: TextView? = null
    private var progressBar: ProgressBar? = null

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
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_EMPLOYEE)
        setMenu(R.menu.menu_edit)
        showButtonBack()
        setListenerArrowBack()

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionExit -> {
                    signOut()
                    true
                }
                R.id.actionEdit -> {
                    setFragmentResult(
                        MainActivity.SHOW_DETAIL_BRANCH,
                        bundleOf(MainActivity.BUNDLE_KEY to true, MainActivity.BRANCH to branch)
                    )
                    true
                }
                else -> {
                    false
                }
            }
        }

        listEmployeeRecyclerView = view.findViewById(R.id.listEmployeeRecyclerView)
        iconImageView = view.findViewById(R.id.iconImageView)
        titleEmployeeTextView = view.findViewById(R.id.titleEmployeeTextView)
        addEmployeeTextView = view.findViewById(R.id.addNewEmployeeTextView)
        addressTextView = view.findViewById(R.id.addressBranchTextView)
        phoneTextView = view.findViewById(R.id.phoneBranchTextView)
        employeeAdapter = EmployeeRecyclerViewAdapter { employee -> editEmployee(employee) }
        listEmployeeRecyclerView?.adapter = employeeAdapter
        progressBar = view.findViewById(R.id.branchProgressBar)

        arguments?.let {
            branch = it.get(BRANCH) as? BranchModel

            branch?.let { it ->
                branchId = it.id
                addressTextView?.text = it.address
                val textPhone = "Тел: ${it.phone}"
                phoneTextView?.text = textPhone
            }
        }

        loadListEmployee(this)

        addEmployeeTextView?.setOnClickListener {
            setFragmentResult(
                MainActivity.SHOW_EMPLOYEE,
                bundleOf(MainActivity.BUNDLE_KEY to true, MainActivity.BRANCH_ID to branchId)
            )
        }
    }
    /**
     * Загружает список сотрудников работающих в этом филиалле
     * */

    private fun loadListEmployee(callBack: BranchCallBack) {
        val weakReferenceCallBack: WeakReference<BranchCallBack> = WeakReference(callBack)
        val childName = getString(R.string.employees_firebase_key)

        val query = reference.child(childName).orderByChild("branchId").equalTo(branchId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listEmployee: MutableList<Employee> = mutableListOf()
                for (child in snapshot.children) {
                    val employee = child.getValue<Employee>()
                    employee?.let { listEmployee.add(it) }
                }
                weakReferenceCallBack.get()?.setListEmployee(listEmployee)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(FIREBASE_LOG_TAG, error.message)
            }
        })
    }

    /**
     * Переходит на фрагмент для изменение данных об филиале
     * */

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

    /**
     * Скрывает ProgressBar
     * */

    private fun goneProgressBar() {
        progressBar?.visibility = View.GONE
    }

    companion object {
        private const val FIREBASE_LOG_TAG = "Firebase"
        private const val EMPLOYEES_FIREBASE_KEY = "employees"
        private const val BRANCH_ID = "branch_id"
        private const val BRANCH = "branch"
        private const val TITLE_EMPLOYEE = "Филиал"

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

    /**
     * Обновляет данные о сотрудниках
     * */

    override fun setListEmployee(listEmployee: List<Employee>) {
        employeeAdapter?.setDateSet(listEmployee)
        goneProgressBar()
        visibleAllViews()
    }

    /**
     * Отображает все виджеты
     * */

    private fun visibleAllViews() {
        iconImageView?.visibility = View.VISIBLE
        addressTextView?.visibility = View.VISIBLE
        phoneTextView?.visibility = View.VISIBLE
        titleEmployeeTextView?.visibility = View.VISIBLE
        addEmployeeTextView?.visibility = View.VISIBLE
    }
}
