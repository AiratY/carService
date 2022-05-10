package ru.airatyunusov.carservice

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import ru.airatyunusov.carservice.model.FirebaseHelper

open class BlankFragment : Fragment() {
    protected val firebaseHelper = FirebaseHelper()
    protected val reference = firebaseHelper.getDatabaseReference()

    /**
     * Возвращает назад
     * */

    protected fun returnBack() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    /**
     * ВОзвращает ID пользователя из SP
     * нужно сделать by lazy
     * */

    protected fun getUserId(): String {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.user_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        val userId = getString(R.string.user_id_key_SP)
        return sharedPreferences.getString(userId, "") ?: ""
    }

    /**
     * Выходит из аккаунта
     * */

    protected fun signOut() {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.user_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        with(sharedPreferences.edit()) {
            putBoolean(MainActivity.AUTH, false)
            apply()
        }

        setFragmentResult(
            MainActivity.SHOW_AUTH,
            bundleOf(MainActivity.BUNDLE_KEY to true)
        )
    }
}
