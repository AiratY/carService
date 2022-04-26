package ru.airatyunusov.carservice

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthorizationFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var loginEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var email: String = ""
    private var password: String = ""

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

        val signInBtn: Button = view.findViewById(R.id.signInButton)
        val createUserBtn: Button = view.findViewById(R.id.createUserButton)
        loginEditText = view.findViewById(R.id.loginEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)

        createUserBtn.setOnClickListener {
            readEmailAndPassword()
            if (checkEmailAndPassword()) {
                createAccount(email, password)
            }
        }

        signInBtn.setOnClickListener {
            readEmailAndPassword()
            if (checkEmailAndPassword()) {
                signInAccount()
            }

            /*else if (email != LOGIN || password != PASSWORD) {
                Toast.makeText(requireContext(), "Неправильные", Toast.LENGTH_SHORT).show()
            } else {
                setFragmentResult(
                    MainActivity.SHOW_ENROLL,
                    bundleOf(MainActivity.BUNDLE_KEY to true)
                )
                val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putBoolean(MainActivity.AUTH, true)
                    apply()
                }
            }*/
        }
    }

    private fun showAdminFragment(user: FirebaseUser) {
        setFragmentResult(
            MainActivity.SHOW_ADMIN_FRAGMENT,
            bundleOf(MainActivity.BUNDLE_KEY to true)
        )
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.admin_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        with(sharedPreferences.edit()) {
            putBoolean(MainActivity.AUTH, true)
            putString(getString(R.string.user_id_key_SP), user.uid)
            apply()
        }
    }

    private fun signInAccount() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    auth.currentUser?.let { showAdminFragment(it) }
                } else {
                    Log.w(TAG, "createUser:fail", task.exception)
                    Toast.makeText(requireContext(), "Неверный логи или пароль", Toast.LENGTH_SHORT)
                        .show()
                }
            }
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
                    auth.currentUser?.let { showAdminFragment(it) }
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "createUser:fail", task.exception)
                    Toast.makeText(requireContext(), "Что пошло не так", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val LOGIN = "user"
        private const val PASSWORD = "user"
        private const val TAG = "FIREBASE_AUTH"
    }
}
