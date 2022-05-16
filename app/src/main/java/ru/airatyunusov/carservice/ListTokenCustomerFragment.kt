package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.callbacks.ListTokenCallBack
import ru.airatyunusov.carservice.model.TokenFirebaseModel
import java.lang.ref.WeakReference

class ListTokenCustomerFragment : BaseFragment(), ListTokenCallBack {

    private var tokenRecyclerViewAdapter: TokenRecyclerViewAdapter? = null

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
        tokenRecyclerViewAdapter = TokenRecyclerViewAdapter { token -> openDetailToken(token) }
        recyclerView.adapter = tokenRecyclerViewAdapter

        loadListCustomerServices(this)
    }

    /**
     * Переходим на фрагмент с детальным описанием записи
     * */

    private fun openDetailToken(token: TokenFirebaseModel) {
        setFragmentResult(
            MainActivity.SHOW_DETAIL_TOKEN,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.TOKEN to token
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
        tokenRecyclerViewAdapter?.setDateSet(listToken)
    }
}
