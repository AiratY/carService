package ru.airatyunusov.carservice

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        findViewById<TextView>(R.id.customerButton).setOnClickListener {
            startActivityWithBundle(CUSTOMER)
        }

        findViewById<TextView>(R.id.employeeButton).setOnClickListener {
            startActivityWithBundle(EMPLOYEE)
        }

        findViewById<TextView>(R.id.adminButton).setOnClickListener {
            startActivityWithBundle(ADMIN)
        }
    }

    private fun startActivityWithBundle(user: String) {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).apply { putExtra(MainActivity.MESSAGE_USER, user) })
    }

    companion object {
        const val CUSTOMER = "customer"
        const val EMPLOYEE = "employee"
        const val ADMIN = "admin"
    }
}