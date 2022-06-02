package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.callbacks.ListTokenCallBack
import ru.airatyunusov.carservice.model.TokenFirebaseModel
import ru.airatyunusov.carservice.token.TokenRecyclerViewAdapter
import ru.airatyunusov.carservice.utils.DateTimeHelper
import java.lang.ref.WeakReference
import java.time.LocalDateTime

open class ListTokenFragment : BaseFragment(), ListTokenCallBack {

    private var startWeek: LocalDateTime? = null
    private var endWeek: LocalDateTime? = null
    private var startWeekCash: LocalDateTime? = null

    private var prevWeekBtn: Button? = null
    private var nextWeekBtn: Button? = null
    private var tokenRecyclerViewAdapter: TokenRecyclerViewAdapter? = null
    private var messageTextView: TextView? = null
    protected var branchId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenRecyclerView: RecyclerView = view.findViewById(R.id.tokenRecyclerView)
        prevWeekBtn = view.findViewById(R.id.prevWeekButton)
        nextWeekBtn = view.findViewById(R.id.nextWeekButton)
        tokenRecyclerViewAdapter = TokenRecyclerViewAdapter { token -> openDetailToken(token) }
        messageTextView = view.findViewById(R.id.messageNullTokenTextView)
        tokenRecyclerView.adapter = tokenRecyclerViewAdapter

        startWeek = getDateTimeStartWeek()
        startWeekCash = startWeek
        endWeek = getDateTimeEndWeek()

        nextWeekBtn?.setOnClickListener {
            setNextStartAndEndWeekDateTime()
            loadListToken(branchId, this)
            enablePrevBtn()
        }

        prevWeekBtn?.setOnClickListener {
            setPrevStartAndEndWeekDateTime()
            loadListToken(branchId, this)
            if (startWeekCash == startWeek) {
                disablePrevBtn()
            }
        }
    }

    private fun openDetailToken(token: TokenFirebaseModel) {
        setFragmentResult(
            MainActivity.SHOW_DETAIL_TOKEN,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.TOKEN to token,
                MainActivity.IS_DELETE_TOKEN to false
            )
        )
    }

    /**
     * Показывает сообщение о том что нет записей на текущей недели
     * */

    private fun visibleNullMessage() {
        messageTextView?.visibility = View.VISIBLE
    }

    /**
     * Скрывает сообщение о том что нет записей на текущей недели
     * */

    private fun goneNullMessage() {
        messageTextView?.visibility = View.GONE
    }

    /**
     * Блокирует кнопку предыдущая неделя
     * */

    private fun disablePrevBtn() {
        prevWeekBtn?.isEnabled = false
    }

    /**
     * Разблокирует кнопеу предыдущая неделя
     * */

    private fun enablePrevBtn() {
        prevWeekBtn?.isEnabled = true
    }

    /**
     * Увеличивыет значения даты и времени начала и окончанияе недели на одну неделю
     **/

    private fun setNextStartAndEndWeekDateTime() {
        startWeek = startWeek?.plusDays(7)
        endWeek = endWeek?.plusDays(7)
    }

    /**
     * Изменяет дату и время начала и конца неделю на предыдущую
     * */
    private fun setPrevStartAndEndWeekDateTime() {
        startWeek = startWeek?.minusDays(7)
        endWeek = endWeek?.minusDays(7)
    }

    /**
     * Загружает из БД список записей к этому сотруднику
     * */

    fun loadListToken(id: String, callBack: ListTokenFragment) {
        val weakReference: WeakReference<ListTokenCallBack> = WeakReference(callBack)

        val tokenQuery =
            reference.child(TOKEN_MODEL_FIREBASE_KEY)
                .orderByChild("startRecordDateTime")
                .startAt(startWeek?.let { DateTimeHelper.convertToStringDateTime(it) })
                .endAt(endWeek?.let { DateTimeHelper.convertToStringDateTime(it) })

        tokenQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listToken: MutableList<TokenFirebaseModel> = mutableListOf()
                for (data in snapshot.children) {
                    val tokenFirebase = data.getValue<TokenFirebaseModel>() ?: TokenFirebaseModel()
                    if (tokenFirebase.branchId == id) {
                        listToken.add(tokenFirebase)
                    }
                }
                weakReference.get()?.setListTokenFirebaseModel(listToken)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Вычисляет дату начала текущей неделя
     * */

    fun getDateTimeStartWeek(): LocalDateTime {
        return LocalDateTime.now().let {
            it.minusDays(it.dayOfWeek.ordinal.toLong()).withHour(0).withMinute(0)
                .withSecond(0).withNano(0)
        }
    }

    /**
     * Вычисляет дату окончание текущей неделя
     * */

    fun getDateTimeEndWeek(): LocalDateTime {
        return LocalDateTime.now().let { // apply
            val diffDayOfWeek = 6 - it.dayOfWeek.ordinal
            it.plusDays(diffDayOfWeek.toLong()).withHour(23).withMinute(59)
                .withSecond(59).withNano(59)
        }
    }

    override fun setListTokenFirebaseModel(listToken: List<TokenFirebaseModel>) {
        if (listToken.isEmpty()) {
            visibleNullMessage()
        } else {
            goneNullMessage()
        }

        tokenRecyclerViewAdapter?.setDateSet(listToken)
    }

    companion object {
        private const val TOKEN_MODEL_FIREBASE_KEY = "tickets"
    }
}
