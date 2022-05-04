package ru.airatyunusov.carservice

import android.content.Context
import androidx.fragment.app.Fragment
import ru.airatyunusov.carservice.model.FirebaseHelper

open class BlankFragment: Fragment() {

    protected val reference = FirebaseHelper().getDatabaseReference()

    /**
     * Возвращает назад
     * */

    protected fun returnBack() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    /**
     * ВОзвращает ID администратора
     * */

    protected fun getAdminId(): String {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.admin_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        val userId = getString(R.string.user_id_key_SP)
        return sharedPreferences.getString(userId, "") ?: ""
    }
}