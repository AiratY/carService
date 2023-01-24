package ru.airatyunusov.carservice.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import ru.airatyunusov.carservice.BaseFragment
import ru.airatyunusov.carservice.MainActivity
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.model.User

class AuthorizationFragment : BaseFragment() {

    private var auth: FirebaseAuth? = null
    private var loginEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var nameEditText: EditText? = null
    private var phoneEditText: EditText? = null
    private var signInBtn: Button? = null
    private var createUserBtn: Button? = null
    private var titleOrTextView: TextView? = null
    private var roleSpinner: Spinner? = null

    private var email: String = ""
    private var password: String = ""
    private var role = ""
    private var isSignIn = false
    private var isRegistr = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth

        return inflater.inflate(R.layout.fragment_authorization, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_AUTH)

        signInBtn = view.findViewById(R.id.signInButton)
        createUserBtn = view.findViewById(R.id.createUserButton)
        loginEditText = view.findViewById(R.id.loginEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        nameEditText = view.findViewById(R.id.nameEditText)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        titleOrTextView = view.findViewById(R.id.titleOrTextView)
        roleSpinner = view.findViewById(R.id.spinnerRole)

        val roleSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, LIST_ROLE
        )
        roleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        roleSpinner?.adapter = roleSpinnerAdapter

        createUserBtn?.setOnClickListener {
            if (isRegistr) {
                readEmailAndPassword()
                if (checkEmailAndPassword() && checkNameAndPhone()) {
                    createAccount(email, password)
                }
            } else {
                isRegistr = true
                visibleLoginAndPasswordEditText()
                visibleRoleSpinner()
                goneSignInBtn()
                goneTitleOr()
                setTitle(TITLE_REGISTRATION)
                showAuthButtonBack()
                showRegistrationView()
            }
        }

        signInBtn?.setOnClickListener {
            if (isSignIn) {
                readEmailAndPassword()
                if (checkEmailAndPassword()) {
                    signInAccount()
                }
            } else {
                isSignIn = true
                visibleLoginAndPasswordEditText()
                goneCreateUserBtn()
                goneTitleOr()
                setTitle(TITLE_SIGN_IN)
                showAuthButtonBack()
            }
        }

        roleSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                role = parent?.getItemAtPosition(position).toString()
                when (role) {
                    ADMIN -> {
                        showAdminView()
                    }
                    CUSTOMER -> {
                        showCustomerView()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.e(TAG_NO_SELECTED, MESSAGE_ERROR_NO_SELECTED)
            }
        }
    }

    /**
     * Осуществляет валидацию полей ИМя/Название и Телефон
     * */

    private fun checkNameAndPhone(): Boolean {
        return when {
            nameEditText?.text.toString().isEmpty() -> {
                Toast.makeText(requireContext(), MESSAGE_NAME_NOT_NULL, Toast.LENGTH_SHORT)
                    .show()
                false
            }
            phoneEditText?.text.toString().length != 11 -> {
                Toast.makeText(
                    requireContext(),
                    MESSAGE_INVALID_NUMBER,
                    Toast.LENGTH_SHORT
                )
                    .show()
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * Показывает кнопку назад и устанавливает listener
     * */

    private fun showAuthButtonBack() {
        showButtonBack()
        toolbar?.setNavigationOnClickListener {
            goneAuthButtonBack()

            setTitle(TITLE_AUTH)
            isRegistr = false
            isSignIn = false

            signInBtn?.visibility = View.VISIBLE
            createUserBtn?.visibility = View.VISIBLE
            titleOrTextView?.visibility = View.VISIBLE

            loginEditText?.visibility = View.GONE
            passwordEditText?.visibility = View.GONE
            nameEditText?.visibility = View.GONE
            phoneEditText?.visibility = View.GONE
            roleSpinner?.visibility = View.GONE
        }
    }

    /**
     * Скрывает кнопку назад
     * */

    private fun goneAuthButtonBack() {
        toolbar?.navigationIcon = null
    }

    /**
     * Показывает поля для регистрации клиента
     * */

    private fun showCustomerView() {
        nameEditText?.hint = CUSTOMER_VALUE_NAME_ET
    }

    /**
     * Показывает необходимые поля для регистрации
     * */

    private fun showRegistrationView() {
        nameEditText?.visibility = View.VISIBLE
        phoneEditText?.visibility = View.VISIBLE
    }

    /**
     * Показывает поля для регистрации администатора
     * */

    private fun showAdminView() {
        nameEditText?.hint = ADMIN_VALUE_NAME_ET
    }

    /**
     * Скрывает текст Или
     * */

    private fun goneTitleOr() {
        titleOrTextView?.visibility = View.GONE
    }

    /**
     * Скрывает кнопку регистрации
     * */

    private fun goneCreateUserBtn() {
        createUserBtn?.visibility = View.GONE
    }

    /**
     * Скрывает кнопку Войти
     * */

    private fun goneSignInBtn() {
        signInBtn?.visibility = View.GONE
    }

    /**
     * Показывает спинер для выбора роли
     * */

    private fun visibleRoleSpinner() {
        roleSpinner?.visibility = View.VISIBLE
    }

    /**
     * ПОказыват поля для ввода логина и пароля
     * */

    private fun visibleLoginAndPasswordEditText() {
        loginEditText?.visibility = View.VISIBLE
        passwordEditText?.visibility = View.VISIBLE
    }
    /**
     * Осуществляет переход на новый фрагмент
     * */

    private fun showNextFragment(role: String) {
        when (role) {
            ADMIN -> {
                setFragmentResult(
                    MainActivity.SHOW_ADMIN_FRAGMENT,
                    bundleOf(MainActivity.BUNDLE_KEY to true)
                )
            }
            CUSTOMER -> {
                setFragmentResult(
                    MainActivity.SHOW_CUSTOMER_FRAGMENT,
                    bundleOf(MainActivity.BUNDLE_KEY to true)
                )
            }
            EMPLOYEE -> {
                setFragmentResult(
                    MainActivity.SHOW_EMPLOYEE_FRAGMENT,
                    bundleOf(MainActivity.BUNDLE_KEY to true)
                )
            }
        }
    }

    /**
     * Сохраняет данные пользователя в SharedPreference
     * */

    private fun saveUserDataInSharedPreference(user: User) {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.user_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        with(sharedPreferences.edit()) {
            putBoolean(MainActivity.AUTH, true)
            putString(getString(R.string.user_id_key_SP), user.id)
            putString(getString(R.string.ROLE_SHARED_PREFERENCE_KEY), user.role)
            putString(getString(R.string.user_name_key_SP), user.name)
            putLong(getString(R.string.user_phone_key_SP), user.phone)
            apply()
        }
    }
    /**
     * Осуществляет вход в акаунт
     * */

    private fun signInAccount() {
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // auth.currentUser?.let { showNextFragment(it) }
                    loadDataUser()
                } else {
                    Toast.makeText(
                        requireContext(),
                        MESSAGE_INCORRECT_LOGIN_AND_PASSWORD,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
    }

    /**
     * Загружаем данные об пользователе
     * */
    private fun loadDataUser() {
        val query =
            reference.child(USERS_FB).orderByChild("id").equalTo(auth?.currentUser?.uid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val user = data.getValue<User>() ?: User()
                    saveUserDataInSharedPreference(user)
                    showNextFragment(user.role)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }
    /**
     * Проверяет валидацию Email и пароля
     * */

    private fun checkEmailAndPassword(): Boolean {
        return if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), MESSAGE_EDIT_TEXT_NOT_NULL, Toast.LENGTH_SHORT)
                .show()
            false
        } else {
            true
        }
    }
    /**
     * Обновляет данные почты и пароля в перменных
     * */

    private fun readEmailAndPassword() {
        email = loginEditText?.text.toString()
        password = passwordEditText?.text.toString()
    }

    /**
     * Регистрирует новый акаунт
     * */

    private fun createAccount(email: String, password: String) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = saveUser()
                    showNextFragment(user.role)
                    saveUserDataInSharedPreference(user)
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
     * Сохраняет данные пользователя в БД
     * */

    private fun saveUser(): User {
        val name = nameEditText?.text.toString()
        val phone = phoneEditText?.text.toString().toLong()

        val user = auth?.uid?.let { User(it, role, name, phone) } ?: User()
        user.saveUser()

        return user
    }

    companion object {
        private const val USERS_FB = "users"
        private const val TITLE_SIGN_IN = "Вход в ЛК"
        private const val TITLE_REGISTRATION = "Регистрация"
        private const val TITLE_AUTH = "Авторизация"

        private const val ADMIN_VALUE_NAME_ET = "Название"
        private const val CUSTOMER_VALUE_NAME_ET = "Имя"

        private const val ADMIN = "Администратор"
        private const val CUSTOMER = "Клиент"
        private const val EMPLOYEE = MainActivity.ROLE_EMPLOYEE

        private val LIST_ROLE: List<String> = listOf(ADMIN, CUSTOMER)

        private const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
        private const val ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
        private const val ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"

        private const val MESSAGE_ERROR_INVALID_EMAIL =
            "Адрес электронной почты имеет неправильный формат"
        private const val MESSAGE_ERROR_WEAK_PASSWORD = "Пароль должен содержать минимум 6 символов"
        private const val MESSAGE_ERROR_EMAIL_ALREADY_IN_USE =
            "Адрес электронной почты уже используется другим аккаунтом."
        private const val MESSAGE_EDIT_TEXT_NOT_NULL = "Поля не должны быть пустыми"
        private const val MESSAGE_INCORRECT_LOGIN_AND_PASSWORD = "Неверный логи или пароль"
        private const val MESSAGE_INVALID_NUMBER = "Номер телефона должен содержать 11 цифр"
        private const val MESSAGE_NAME_NOT_NULL = "Поле не должно быть пустым"
        private const val MESSAGE_ERROR_NO_SELECTED = "Ничего не выбранно"
        private const val TAG_NO_SELECTED = "NO_SELECTED"
    }
}
