package ru.airatyunusov.carservice

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import ru.airatyunusov.carservice.model.FirebaseHelper
import ru.airatyunusov.carservice.model.User

class AuthorizationFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
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
    private var isCheck = false

    private val reference = FirebaseHelper().getDatabaseReference()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth

        return inflater.inflate(R.layout.fragment_authorization, container, false)
    }

    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        /*val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }*/
    }

    private fun reload() {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            if (isCheck) {
                readEmailAndPassword()
                if (checkEmailAndPassword()) {
                    createAccount(email, password)
                }
            } else {
                isCheck = true
                visibleLoginAndPasswordEditText()
                visibleRoleSpinner()
                goneSignInBtn()
                goneTitleOr()
            }
        }

        signInBtn?.setOnClickListener {
            if (isCheck) {
                readEmailAndPassword()
                if (checkEmailAndPassword()) {
                    signInAccount()
                }
            } else {
                isCheck = true
                visibleLoginAndPasswordEditText()
                goneCreateUserBtn()
                goneTitleOr()
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
                Log.e("SELECTED", "Ничего не выбранно")
            }
        }
    }

    /**
     * Показывает поля для регистрации клиента
     * */

    private fun showCustomerView() {
        nameEditText?.visibility = View.VISIBLE
        nameEditText?.hint = "Имя"
        phoneEditText?.visibility = View.VISIBLE
    }

    /**
     * Показывает поля для регистрации администатора
     * */

    private fun showAdminView() {
        nameEditText?.visibility = View.VISIBLE
        nameEditText?.hint = "Название"
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
     * Скрывает кнопку Ввойти
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

    private fun signInAccount() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    // auth.currentUser?.let { showNextFragment(it) }
                    loadDataUser()
                } else {
                    Log.w(TAG, "createUser:fail", task.exception)
                    Toast.makeText(requireContext(), "Неверный логи или пароль", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    /**
     * Загружаем данные об пользователе
     * */
    private fun loadDataUser() {
        val query =
            reference.child(USERS_FB).orderByChild("id").equalTo(auth.currentUser?.uid)

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

    private fun checkEmailAndPassword(): Boolean {
        return if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Пустые", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun readEmailAndPassword() {
        email = loginEditText?.text.toString()
        password = passwordEditText?.text.toString()
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = saveUser()
                    showNextFragment(user.role)
                    saveUserDataInSharedPreference(user)
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "createUser:fail", task.exception)
                    Toast.makeText(requireContext(), "Что пошло не так", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Сохраняет данные пользователя в БД
     * */

    private fun saveUser(): User {
        val name = nameEditText?.text.toString()
        val phone = phoneEditText?.text.toString().toLong()

        val user = auth.uid?.let { User(it, role, name, phone) } ?: User()
        // reference.child(USERS_FB).push().setValue(user)
        user.saveUser()

        return user
    }

    companion object {
        private const val TAG = "FIREBASE_AUTH"
        private const val USERS_FB = "users"

        private const val ADMIN = "Администратор"
        private const val CUSTOMER = "Клиент"
        private const val EMPLOYEE = MainActivity.ROLE_EMPLOYEE

        private val LIST_ROLE: List<String> = listOf(ADMIN, CUSTOMER)
    }
}
