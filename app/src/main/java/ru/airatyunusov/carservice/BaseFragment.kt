package ru.airatyunusov.carservice

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import ru.airatyunusov.carservice.model.FirebaseHelper

open class BaseFragment : Fragment() {
    protected val firebaseHelper = FirebaseHelper()
    protected val reference = firebaseHelper.getDatabaseReference()

    private var titleToolbar: TextView? = null
    protected var toolbar: Toolbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleToolbar = view.findViewById(R.id.titleToolbar)
        toolbar = view.findViewById(R.id.toolbar)

        setMenu(R.menu.menu_admin_page)

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionExit -> {
                    signOut()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    /**
     * Устанавливает кнопку назад
     * */

    fun showButtonBack() {
        toolbar?.navigationIcon = resources.getDrawable(R.drawable.ic_baseline_arrow_back_24)
        toolbar?.setNavigationOnClickListener {
            returnBack()
            // (requireActivity() as? AppCompatActivity)?.onBackPressed()
        }
    }

    /**
     * Устанавливает заголовок для toolbar
     **/

    protected fun setTitle(title: String) {
        titleToolbar?.text = title
    }

    /**
     * Устанавливает layout для меню
     **/

    protected fun setMenu(menu: Int) {
        toolbar?.inflateMenu(menu)
    }

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
