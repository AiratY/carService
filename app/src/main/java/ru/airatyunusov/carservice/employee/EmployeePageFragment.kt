package ru.airatyunusov.carservice.employee

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.*
import ru.airatyunusov.carservice.callbacks.EmployeePageCallBack
import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.TokenFirebaseModel
import ru.airatyunusov.carservice.token.TokenRecyclerViewAdapter
import ru.airatyunusov.carservice.utils.DateTimeHelper
import java.lang.ref.WeakReference
import java.time.LocalDateTime

class EmployeePageFragment : BaseFragment(), EmployeePageCallBack {

    private var prevWeekBtn: Button? = null
    private var nextWeekBtn: Button? = null
    private var fullNameTextView: TextView? = null
    private var tokenRecyclerViewAdapter: TokenRecyclerViewAdapter? = null
    private var employeeId = ""
    private var startWeek: LocalDateTime? = null
    private var endWeek: LocalDateTime? = null
    private var startWeekCash: LocalDateTime? = null
    private var progressBar: ProgressBar? = null
    private var messageTextView: TextView? = null
    private var phoneTextView: TextView? = null

    private var listAllToken: List<TokenFirebaseModel> = emptyList()
    private var listExpectedToken: List<TokenFirebaseModel> = emptyList()
    private var isShowAllToken = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employee_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOOLBAR)
        setMenuWithExit()

        progressBar = view.findViewById(R.id.employeeProgressBar)
        fullNameTextView = view.findViewById(R.id.fullNameTextView)
        val tokenRecyclerView: RecyclerView = view.findViewById(R.id.tokenRecyclerView)
        prevWeekBtn = view.findViewById(R.id.prevWeekButton)
        nextWeekBtn = view.findViewById(R.id.nextWeekButton)
        tokenRecyclerViewAdapter = TokenRecyclerViewAdapter(requireContext()) { token -> openDetailToken(token) }
        tokenRecyclerView.adapter = tokenRecyclerViewAdapter
        messageTextView = view.findViewById(R.id.messageNullTokenTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        val switch: SwitchCompat = view.findViewById(R.id.switchOldTickets)

        startWeek = getDateTimeStartWeek()
        startWeekCash = startWeek
        endWeek = getDateTimeEndWeek()

        employeeId = getUserId()

        loadDataEmployee(this)
        loadListToken(this)

        nextWeekBtn?.setOnClickListener {
            setNextStartAndEndWeekDateTime()
            loadListToken(this)
            enablePrevBtn()
        }

        prevWeekBtn?.setOnClickListener {
            setPrevStartAndEndWeekDateTime()
            loadListToken(this)
            if (startWeekCash == startWeek) {
                disablePrevBtn()
            }
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            isShowAllToken = isChecked
            if (isChecked) {
                showAllListToken()
            } else {
                showExpectedToken()
            }
        }
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
     * Загружает данные о текущкм сотрудники из БД
     * */

    private fun loadDataEmployee(employeePageCallBack: EmployeePageCallBack) {
        val callBack: WeakReference<EmployeePageCallBack> = WeakReference(employeePageCallBack)

        val query =
            reference.child("employees").orderByChild("id").equalTo(employeeId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val employee = data.getValue<Employee>() ?: Employee()
                    callBack.get()?.setDataEmployee(employee)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Загружает из БД список записей к этому сотруднику
     * */

    private fun loadListToken(employeePageCallBack: EmployeePageCallBack) {

        visibleProgressBar()

        val callBack: WeakReference<EmployeePageCallBack> = WeakReference(employeePageCallBack)

        val tokenQuery =
            reference.child(TOKEN_MODEL_FIREBASE_KEY).orderByChild("startRecordDateTime")
                .startAt(startWeek?.let { DateTimeHelper.convertToStringDateTime(it) })
                .endAt(endWeek?.let { DateTimeHelper.convertToStringDateTime(it) })

        tokenQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listToken: MutableList<TokenFirebaseModel> = mutableListOf()
                for (data in snapshot.children) {
                    val tokenFirebase = data.getValue<TokenFirebaseModel>() ?: TokenFirebaseModel()
                    if (tokenFirebase.idEmployee == employeeId) {
                        listToken.add(tokenFirebase)
                    }
                }
                callBack.get()?.setListTokenFirebaseModel(listToken)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Вычисляет дату начала текущей неделя
     * */

    private fun getDateTimeStartWeek(): LocalDateTime {
        return LocalDateTime.now().let { // apply
            it.minusDays(it.dayOfWeek.ordinal.toLong()).withHour(0).withMinute(0)
                .withSecond(0).withNano(0)
        }
    }

    /**
     * Вычисляет дату окончание текущей неделя
     * */

    private fun getDateTimeEndWeek(): LocalDateTime {
        return LocalDateTime.now().let { // apply
            val diffDayOfWeek = 6 - it.dayOfWeek.ordinal
            it.plusDays(diffDayOfWeek.toLong()).withHour(23).withMinute(59)
                .withSecond(59).withNano(59)
        }
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
                MainActivity.IS_DELETE_TOKEN to false
            )
        )
    }

    companion object {
        private const val TOKEN_MODEL_FIREBASE_KEY = "tickets"
        private const val TITLE_TOOLBAR = "Профиль сотрудника"
    }

    /**
     * Обновляет данные в RecyclerView
     * */

    override fun setListTokenFirebaseModel(list: List<TokenFirebaseModel>) {
        goneProgressBar()

        if (list.isEmpty()) {
            visibleNullMessage()
            tokenRecyclerViewAdapter?.setDateSet(list)
            listAllToken = list
            listExpectedToken = list
        } else {
            goneNullMessage()

            listAllToken = list.sortedByDescending { it.startRecordDateTime }

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
     * Показывает ProgressBar
     * */

    private fun visibleProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    /**
     * Скрывает ProgressBar
     * */

    private fun goneProgressBar() {
        progressBar?.visibility = View.GONE
    }

    /**
     * Обновляет TextView
     * */

    override fun setDataEmployee(employee: Employee) {
        val fullName = "${employee.lastName} ${employee.firstName} ${employee.patronymic}"
        fullNameTextView?.text = fullName
        phoneTextView?.text = employee.phone.toString()
    }
}
