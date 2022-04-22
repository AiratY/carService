package ru.airatyunusov.carservice

import android.content.Context
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

class AuthorizationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_authorization, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signInBtn: Button = view.findViewById(R.id.signInButton)
        val loginEditText: EditText = view.findViewById(R.id.loginEditText)
        val passwordEditText: EditText = view.findViewById(R.id.passwordEditText)

        signInBtn.setOnClickListener {
            val login: String = loginEditText.text.toString()
            val password: String = passwordEditText.text.toString()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Пустые", Toast.LENGTH_SHORT).show()
            } else if (login != LOGIN || password != PASSWORD) {
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
            }
        }
    }

    companion object {
        private const val LOGIN = "user"
        private const val PASSWORD = "user"
    }
}
