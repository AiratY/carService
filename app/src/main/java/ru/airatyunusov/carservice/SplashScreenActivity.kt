package ru.airatyunusov.carservice

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, TIME_PAUSE)

        /*findViewById<TextView>(R.id.customerButton).setOnClickListener {
            startActivityWithBundle(CUSTOMER)
        }

        findViewById<TextView>(R.id.employeeButton).setOnClickListener {
            startActivityWithBundle(EMPLOYEE)
        }

        findViewById<TextView>(R.id.adminButton).setOnClickListener {
            startActivityWithBundle(ADMIN)
        }*/
    }

    private fun startActivityWithBundle(user: String) {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
        )

        /*startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).apply { putExtra(MainActivity.MESSAGE_USER, user) }
        )*/
    }
    companion object {

        private const val TIME_PAUSE: Long = 3000
    }
}
