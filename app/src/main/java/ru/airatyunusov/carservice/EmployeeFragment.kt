package ru.airatyunusov.carservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.User

class EmployeeFragment : BlankFragment() {

    private var branchId = ""
    private var employee: Employee? = null
    private var deleteBtn: Button? = null
    private var loginEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var childName = ""

    private var firstName: String = ""
    private var lastName: String = ""
    private var patronymic: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childName = getString(R.string.employees_firebase_key)

        val firstNameEditText: EditText = view.findViewById(R.id.nameEmployeeEditText)
        val lastNameEditText: EditText = view.findViewById(R.id.lastNameEditText)
        val patronymicEditText: EditText = view.findViewById(R.id.patronymicEditText)
        val saveBtn: Button = view.findViewById(R.id.saveEmployeeButton)
        loginEditText = view.findViewById(R.id.loginEmployeeEditText)
        passwordEditText = view.findViewById(R.id.passwordEmployeeEditText)
        deleteBtn = view.findViewById(R.id.deleteEmployeeBtn)

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

        if (employee == null) {
            visibleLoginAndPasswordEditText()
        }

        deleteBtn?.setOnClickListener {
            removeEmployee()
            returnBack()
        }

        saveBtn.setOnClickListener {
            firstName = firstNameEditText.text.toString()
            lastName = lastNameEditText.text.toString()
            patronymic = patronymicEditText.text.toString()
            val login: String = loginEditText?.text.toString()
            val password: String = passwordEditText?.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || patronymic.isEmpty() || login.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Поля для ввода не должны бысь пустыми",
                    Toast.LENGTH_LONG
                ).show()
            } else if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Пароль должен содержать минимум 6 символов",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (employee == null) {
                    firebaseHelper.createAccount(
                        requireActivity(),
                        login,
                        password
                    ) { id -> saveDataEmployee(id) }
                    // saveEmployee(firstName, lastName, patronymic, login, password)
                } else {
                    updateEmployee(firstName, lastName, patronymic)
                }
                returnBack()
            }
        }
    }

    /**
     * Показывает поля для ввода логина и пароля
     * */

    private fun visibleLoginAndPasswordEditText() {
        loginEditText?.visibility = View.VISIBLE
        passwordEditText?.visibility = View.VISIBLE
    }

    /**
     * Удаление данных о сотруднике из БД
     * */

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

    private fun visibleDeleteBtn() {
        deleteBtn?.visibility = View.VISIBLE
    }

    /*private fun returnOnBranchFragment() {
        setFragmentResult(
            MainActivity.SHOW_ADMIN_FRAGMENT,
            bundleOf(MainActivity.BUNDLE_KEY to true)
        )
    }*/

    /**
     * Сохраняет в БД данные об сотрудники
     */

    private fun saveDataEmployee(id: String) {
        updateEmployeeInFireBase(id, branchId, firstName, lastName, patronymic)
        val user = User(id = id, role = MainActivity.ROLE_EMPLOYEE, name = firstName)

        /*user.saveUserDataInSharedPreference(requireActivity(), resources)*/

        user.saveUser()

        /*try {
            val reference = FirebaseHelper().getDatabaseReference()

            reference.child("users").push().setValue(user)
        } catch (ex: InvocationTargetException) {
            Log.e("Check", ex.cause.toString())
        } catch (ex: Exception) {
            Log.e("Check", ex.cause?.printStackTrace().toString())
        }*/
    }

    /**
     * Обновление данных сотрудника в БД
     * */

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

    /**
     * Сохранение сотрудника в БД
     * */

    private fun saveEmployee(
        firstName: String,
        lastName: String,
        patronymic: String,
        login: String,
        password: String
    ) {

        val key = reference.child(childName).push().key // получить значение из БД
        key?.let {
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
