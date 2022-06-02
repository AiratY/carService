package ru.airatyunusov.carservice

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseHelper {

    private val database: FirebaseDatabase =
        Firebase.database("https://carservice-93ef9-default-rtdb.europe-west1.firebasedatabase.app/")
    private var auth: FirebaseAuth = Firebase.auth

    fun getDatabaseReference(): DatabaseReference {
        return database.reference
    }

    /**
     * Создаёт пользователя в БД
     **/

    fun createAccount(
        activity: Activity,
        email: String,
        password: String,
        onClick: ((String) -> Unit)
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.uid?.let { onClick(it) }
                } else {
                    Log.w(TAG, FAIL_MSG, task.exception)
                }
            }
    }

    companion object {
        private const val TAG = "FIREBASE_AUTH"
        private const val FAIL_MSG = "createUser:fail"
    }
}
