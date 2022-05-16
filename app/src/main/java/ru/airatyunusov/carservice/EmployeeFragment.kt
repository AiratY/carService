package ru.airatyunusov.carservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.User

class EmployeeFragment : BaseFragment() {

    private var auth: FirebaseAuth? = null
    private var branchId = ""
    private var employee: Employee? = null
    private var deleteBtn: Button? = null
    private var loginEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var firstNameEditText: EditText? = null
    private var lastNameEditText: EditText? = null
    private var patronymicEditText: EditText? = null
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
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        setTitle("Сотрудник")
        showButtonBack()
        setListenerArrowBack()

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionExit -> {
                    signOut()
                    true
                }
                R.id.actionDelete -> {
                    removeEmployee()
                    returnBack()
                    true
                }
                R.id.actionOk -> {
                    prepareToSaveEmployees()
                    true
                }
                else -> {
                    false
                }
            }
        }

        childName = getString(R.string.employees_firebase_key)

        firstNameEditText = view.findViewById(R.id.nameEmployeeEditText)
        lastNameEditText = view.findViewById(R.id.lastNameEditText)
        patronymicEditText = view.findViewById(R.id.patronymicEditText)

        loginEditText = view.findViewById(R.id.loginEmployeeEditText)
        passwordEditText = view.findViewById(R.id.passwordEmployeeEditText)

        arguments?.let {
            branchId = it.getString(BRANCH_ID) ?: ""
            employee = it.get(EMPLOYEE) as Employee?
        }

        employee?.apply {
            firstNameEditText?.setText(firstName)
            lastNameEditText?.setText(lastName)
            patronymicEditText?.setText(patronymic)
            val textNull = "null"
            loginEditText?.setText(textNull)
            passwordEditText?.setText(textNull)
            visibleDeleteBtn()
            this@EmployeeFragment.branchId = branchId
        }

        if (employee == null) {
            setMenu(R.menu.menu_save)
            visibleLoginAndPasswordEditText()
        } else {
            setMenu(R.menu.menu_save_delete)
        }
    }

    private fun prepareToSaveEmployees() {
        firstName = firstNameEditText?.text.toString()
        lastName = lastNameEditText?.text.toString()
        patronymic = patronymicEditText?.text.toString()
        val login: String = loginEditText?.text.toString()
        val password: String = passwordEditText?.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || patronymic.isEmpty() || login.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                requireContext(),
                MESSAGE_EDIT_TEXT_NOT_NULL,
                Toast.LENGTH_LONG
            ).show()
        } else {
            if (employee == null) {

                createAccount(login, password)
            } else {
                updateEmployee(firstName, lastName, patronymic)
            }
            // returnBack()
        }
    }

    /**
     * Регистрирует новый акаунт
     * */

    private fun createAccount(email: String, password: String) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val id = auth?.currentUser?.uid ?: ""
                    updateEmployeeInFireBase(id, branchId, firstName, lastName, patronymic)
                    val user = User(id = id, role = MainActivity.ROLE_EMPLOYEE, name = firstName)
                    user.saveUser()
                } else {
                    val message = when ((task.exception as? FirebaseAuthException)?.errorCode) {
                        ERROR_INVALID_EMAIL -> MESSAGE_ERROR_INVALID_EMAIL
                        ERROR_WEAK_PASSWORD -> MESSAGE_ERROR_WEAK_PASSWORD
                        ERROR_EMAIL_ALREADY_IN_USE -> MESSAGE_ERROR_EMAIL_ALREADY_IN_USE
                        else -> task.exception?.message
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

        returnBack()
    }

    companion object {
        private const val BRANCH_ID = "branch_id"
        private const val EMPLOYEE = "employee"

        private const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
        private const val ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
        private const val ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"

        private const val MESSAGE_ERROR_INVALID_EMAIL =
            "Адрес электронной почты имеет неправильный формат"
        private const val MESSAGE_ERROR_WEAK_PASSWORD = "Пароль должен содержать минимум 6 символов"
        private const val MESSAGE_ERROR_EMAIL_ALREADY_IN_USE =
            "Адрес электронной почты уже используется другим аккаунтом."

        private const val MESSAGE_EDIT_TEXT_NOT_NULL = "Поля не должны быть пустыми"

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
