package ru.airatyunusov.carservice.model

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import ru.airatyunusov.carservice.MainActivity
import ru.airatyunusov.carservice.R
import java.lang.ref.WeakReference

data class User(
    var id: String = "",
    var role: String = "",
    var name: String = "",
    var phone: Long = 0,
) {

    /**
     * Сохраняет данные пользователя в БД
     * */

    fun saveUser() {
        val reference = FirebaseHelper().getDatabaseReference()
        reference.child(USERS_FB).push().setValue(this)
    }

    /**
     * Сохраняет данные пользователя в SharedPreference
     * */

    fun saveUserDataInSharedPreference(activity: Activity, resource: Resources) {
        val activity: WeakReference<Activity> = WeakReference(activity)
        val resources: WeakReference<Resources> = WeakReference(resource)
        val sharedPreferences = activity.get()?.getSharedPreferences(
            resources.get()?.getString(R.string.user_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        sharedPreferences?.edit()?.apply {
            putBoolean(ru.airatyunusov.carservice.MainActivity.AUTH, true)
            putString(
                resources.get()?.getString(ru.airatyunusov.carservice.R.string.user_id_key_SP),
                id
            )
            putString(
                resources.get()
                    ?.getString(ru.airatyunusov.carservice.R.string.ROLE_SHARED_PREFERENCE_KEY),
                role
            )
            putString(
                resources.get()
                    ?.getString(ru.airatyunusov.carservice.R.string.user_name_key_SP),
                name
            )
            putLong(
                resources.get()
                    ?.getString(ru.airatyunusov.carservice.R.string.user_phone_key_SP),
                phone
            )
            apply()
        }
    }

    companion object {
        private const val USER_DATA_SP = "user_data"
        private const val USERS_FB = "users"
    }
}
