package ru.airatyunusov.carservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.FirebaseHelper

class EmployeeFragment : Fragment() {

    private var branchId = ""
    private var employee: Employee? = null
    private var deleteBtn: Button? = null
    private var childName = ""
    private val reference = FirebaseHelper().getDatabaseReference()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childName = getString(R.string.employees_firebase_key)

        val firstNameEditText: EditText = view.findViewById(R.id.nameEmployeeEditText)
        val lastNameEditText: EditText = view.findViewById(R.id.lastNameEditText)
        val patronymicEditText: EditText = view.findViewById(R.id.patronymicEditText)
        val saveBtn: Button = view.findViewById(R.id.saveEmployeeButton)
        deleteBtn = view.findViewById(R.id.deleteEmployeeBtn)

        goneDeleteBtn()

        arguments?.let {
            branchId = it.getString(BRANCH_ID) ?: ""
            employee = it.get(EMPLOYEE) as Employee?
        }

        employee?.apply {
            firstNameEditText.setText(firstName)
            lastNameEditText.setText(lastName)
            patronymicEditText.setText(patronymic)
            visibleDeleteBtn()
            this@EmployeeFragment.branchId = branchId
        }

        deleteBtn?.setOnClickListener {
            removeEmployee()
            returnOnBranchFragment()
        }

        saveBtn.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val patronymic: String = patronymicEditText.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || patronymic.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Поля для ввода не должны бысь пустыми",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (employee == null) {
                    saveEmployee(firstName, lastName, patronymic)
                } else {
                    updateEmployee(firstName, lastName, patronymic)
                }
                returnOnBranchFragment()
            }
        }
    }

    private fun removeEmployee() {
        val key = employee?.id
        key?.let {
            reference.child(childName).child(it).removeValue()
        }
    }

    private fun updateEmployee(firstName: String, lastName: String, patronymic: String) {
        val key = employee?.id
        val branchId = employee?.branchId ?: ""
        key?.let {
            updateEmployeeInFireBase(it, branchId, firstName, lastName, patronymic)
        }
    }

    private fun goneDeleteBtn() {
        deleteBtn?.visibility = View.GONE
    }

    private fun visibleDeleteBtn() {
        deleteBtn?.visibility = View.VISIBLE
    }

    private fun returnOnBranchFragment() {
        setFragmentResult(
            MainActivity.SHOW_ADMIN_FRAGMENT,
            bundleOf(MainActivity.BUNDLE_KEY to true)
        )
    }

    private fun updateEmployeeInFireBase(
        key: String,
        branchId: String,
        firstName: String,
        lastName: String,
        patronymic: String
    ) {
        val employee = Employee(key, branchId, firstName, lastName, patronymic)
        val childUpdates = hashMapOf<String, Any>(
            "/$childName/$key" to employee
        )
        reference.updateChildren(childUpdates)
    }

    private fun saveEmployee(firstName: String, lastName: String, patronymic: String) {
        val key = reference.child(childName).push().key
        key?.let {
            updateEmployeeInFireBase(it, branchId, firstName, lastName, patronymic)
        }
    }

    companion object {
        private const val BRANCH_ID = "branch_id"
        private const val EMPLOYEE = "employee"

        fun newInstance(branchId: String): EmployeeFragment {
            return EmployeeFragment().apply {
                arguments = bundleOf(BRANCH_ID to branchId)
            }
        }

        fun newInstance(employee: Employee): EmployeeFragment {
            return EmployeeFragment().apply {
                arguments = bundleOf(EMPLOYEE to employee)
            }
        }
    }
}
