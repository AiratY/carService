package ru.airatyunusov.carservice.customer.services

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.BaseFragment
import ru.airatyunusov.carservice.MainActivity
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.callbacks.ListTokenCallBack
import ru.airatyunusov.carservice.model.TokenFirebaseModel
import ru.airatyunusov.carservice.token.TokenRecyclerViewAdapter
import java.lang.ref.WeakReference
import java.time.LocalDateTime

class ListTokenCustomerFragment : BaseFragment(), ListTokenCallBack {

    private var tokenRecyclerViewAdapter: TokenRecyclerViewAdapter? = null
    private var listAllToken: List<TokenFirebaseModel> = emptyList()
    private var listExpectedToken: List<TokenFirebaseModel> = emptyList()
    private var isShowAllToken = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_token_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOOLBAR)
        setMenuWithExit()

        val recyclerView: RecyclerView = view.findViewById(R.id.listMyTokenRecyclerView)
        val switch: SwitchCompat = view.findViewById(R.id.switchOldTickets)

        tokenRecyclerViewAdapter = TokenRecyclerViewAdapter(requireContext()) { token -> openDetailToken(token) }
        recyclerView.adapter = tokenRecyclerViewAdapter

        switch.setOnCheckedChangeListener { _, isChecked ->
            isShowAllToken = isChecked
            if (isChecked) {
                showAllListToken()
            } else {
                showExpectedToken()
            }
        }

        loadListCustomerServices(this)
    }

    /**
     * Показывает весь список записей
     * */

    private fun showAllListToken() {
        tokenRecyclerViewAdapter?.setDateSet(listAllToken)
    }

    /**
     * Показывает список только ожидающихся записей
     * */

    private fun showExpectedToken() {
        tokenRecyclerViewAdapter?.setDateSet(listExpectedToken)
    }

    /**
     * Переходим на фрагмент с детальным описанием записи
     * */

    private fun openDetailToken(token: TokenFirebaseModel) {
        setFragmentResult(
            MainActivity.SHOW_DETAIL_TOKEN,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.TOKEN to token,
                MainActivity.IS_DELETE_TOKEN to true
            )
        )
    }

    /**
     * Загружает список записей клиента
     * */

    private fun loadListCustomerServices(callback: ListTokenCallBack) {
        val weakReference = WeakReference(callback)
        val listToken: MutableList<TokenFirebaseModel> = mutableListOf()

        val tokenQuery =
            reference.child(TOKEN_MODEL_FIREBASE_KEY).orderByChild("userId").equalTo(getUserId())

        tokenQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val tokenFirebase = data.getValue<TokenFirebaseModel>() ?: TokenFirebaseModel()
                    listToken.add(tokenFirebase)
                }
                weakReference.get()?.setListTokenFirebaseModel(listToken)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    override fun isShowBottomNavigationView(): Boolean {
        return true
    }

    companion object {
        private const val TOKEN_MODEL_FIREBASE_KEY = "tickets"
        private const val TITLE_TOOLBAR = "Мои записи"
    }

    override fun setListTokenFirebaseModel(listToken: List<TokenFirebaseModel>) {

        listAllToken = listToken.sortedByDescending { it.startRecordDateTime }

        val mutListExpectedToken: MutableList<TokenFirebaseModel> = mutableListOf()

        val dateNow = LocalDateTime.now()

        for (token in listAllToken) {
            if (LocalDateTime.parse(token.endRecordDateTime) > dateNow) {
                mutListExpectedToken.add(token)
            }
        }

        listExpectedToken = mutListExpectedToken
        if (isShowAllToken) {
            showAllListToken()
        } else {
            showExpectedToken()
        }
    }
}
