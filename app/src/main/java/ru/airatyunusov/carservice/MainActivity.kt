package ru.airatyunusov.carservice

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import ru.airatyunusov.carservice.model.ServiceModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val textView: TextView = findViewById(R.id.textView)

        when (intent.getStringExtra(MESSAGE_USER)) {
            SplashScreenActivity.CUSTOMER -> textView.text = SplashScreenActivity.CUSTOMER
            SplashScreenActivity.EMPLOYEE -> textView.text = SplashScreenActivity.EMPLOYEE
            SplashScreenActivity.ADMIN -> textView.text = SplashScreenActivity.ADMIN
        }*/

        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        if (sharedPreferences.contains(AUTH) && sharedPreferences.getBoolean(AUTH, false)) {
            replaceFragment(EnrollFragment())
        } else {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<AuthorizationFragment>(R.id.fragment_container_view)
                addToBackStack(NAME_BACK_STACK)
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_ENROLL, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                replaceFragment(EnrollFragment())
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_SELECT_DATE_TIME, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val listServicesModel: List<ServiceModel> =
                    bundle.get(EnrollFragment.LIST_SERVICE) as? List<ServiceModel> ?: emptyList()
                replaceFragment(SelectDateTimeFragment.newInstance(listServicesModel))
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, fragment)
            addToBackStack(NAME_BACK_STACK)
        }
    }

    companion object {
        private const val NAME_BACK_STACK = "fragments"
        const val MESSAGE_USER = "action"
        const val SHOW_ENROLL = "show enroll"
        const val SHOW_SELECT_DATE_TIME = "show_select_date_time"
        const val BUNDLE_KEY = "show"

        const val AUTH = "is_auth"
    }
}
