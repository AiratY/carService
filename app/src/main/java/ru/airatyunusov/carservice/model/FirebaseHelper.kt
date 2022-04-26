package ru.airatyunusov.carservice.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseHelper {

    private val database: FirebaseDatabase

    init {
        database = Firebase.database("https://carservice-93ef9-default-rtdb.europe-west1.firebasedatabase.app/")
    }
    fun getDatabaseReference(): DatabaseReference {
        return database.reference
    }
}
