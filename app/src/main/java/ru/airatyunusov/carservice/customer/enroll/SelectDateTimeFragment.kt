package ru.airatyunusov.carservice.customer.enroll

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.*
import ru.airatyunusov.carservice.callbacks.EnrollCallBack
import ru.airatyunusov.carservice.model.*
import ru.airatyunusov.carservice.utils.DateTimeHelper
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.util.concurrent.Executors

class SelectDateTimeFragment : BaseFragment(), EnrollCallBack {

    private var listEmployeeSpinner: Spinner? = null
    private var nextWeekBtn: Button? = null
    private var prevWeekBtn: Button? = null
    private var progressBar: ProgressBar? = null
    private var titleSelectEmployeeTextView: TextView? = null
    private var titleSelectDateTimeTextView: TextView? = null
    private var messageTextView: TextView? = null
    private var timeCompleteTextView: TextView? = null
    private var listTokenRecyclerView: RecyclerView? = null
    private var titleCompleteTextView: TextView? = null

    private var timeStart: LocalTime = LocalTime.of(8, 0)
    private var timeEnd: LocalTime = LocalTime.of(20, 0)
    private var diffTimeWork: Int = 0
    private var listServiceModel: List<ServiceModel> = emptyList()
    private var listEmployee: List<Employee> = emptyList()
    private var dayExecuteServices = 0
    private var hoursCompleteTemp = 0
    private val myExecutor: EnrollExecutor = EnrollExecutor(this)

    private var startWeekCash: LocalDateTime = LocalDateTime.now()
    private var startWeek: LocalDateTime = LocalDateTime.now()
    private var endWeek: LocalDateTime = LocalDateTime.now()

    private var selectedEmployee: Employee? = null
    private var selectedToken: TokenModel? = null

    private var listTokenRecyclerAdapter: SelectTokenRecyclerView? = null

    private var carId = ""
    private var branchId = ""
    private var price = 0L
    private var phone = ""
    private var nameCategory = ""
    private var allHoursComplete = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_date_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOOLBAR)
        setMenu(R.menu.menu_save)
        showButtonBack()
        setListenerArrowBack()

        listEmployeeSpinner = view.findViewById(R.id.listEmployeeSpinner)
        nextWeekBtn = view.findViewById(R.id.nextWeekButton)
        prevWeekBtn = view.findViewById(R.id.prevWeekButton)
        progressBar = view.findViewById(R.id.progressBar)
        titleSelectDateTimeTextView = view.findViewById(R.id.selectDateTimeTextView)
        titleSelectEmployeeTextView = view.findViewById(R.id.titleSelectEmployeeTextView)
        timeCompleteTextView = view.findViewById(R.id.timeCompleteTextView)
        titleCompleteTextView = view.findViewById(R.id.titleCompleteTextView)

        listTokenRecyclerView = view.findViewById(R.id.listTokenRecyclerView)
        listTokenRecyclerAdapter =
            SelectTokenRecyclerView { tokenModel -> setTokenModel(tokenModel) }
        listTokenRecyclerView?.adapter = listTokenRecyclerAdapter

        messageTextView = view.findViewById(R.id.messageTextView)

        goneViews()

        /**
         * Нужно учитывать выходные у сотрудников и праздничные дни
         * Сделать минмальную единицу 30 мин.
         */

        arguments?.let {
            listServiceModel = it.get(LIST_SERVICE) as? List<ServiceModel> ?: emptyList()
            carId = it.getString(CAR_ID, "")
            val branchModel = it.get(BRANCH) as? BranchModel ?: BranchModel()
            branchId = branchModel.id
            phone = branchModel.phone
            price = it.getLong(PRICE, 0)

            nameCategory = it.getString(NAME_CATEGORY, "")

            timeStart = LocalTime.parse(branchModel.startTime)
            timeEnd = LocalTime.parse(branchModel.endTime)
            diffTimeWork = timeEnd.hour - timeStart.hour

            disablePrevBtn()

            myExecutor.executeOnThisWeek()

            nextWeekBtn?.setOnClickListener {
                goneViews()
                enablePrevBtn()
                updateStartEndWeekDateTime()
                myExecutor.executeOnNextWeek()
            }

            prevWeekBtn?.setOnClickListener {
                goneViews()
                minusWeekStartAndEndDateTime()
                myExecutor.executeOnNextWeek()
            }
        }

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionOk -> {
                    selectedToken?.let { token ->
                        registryNewTokenInDB(token, listServiceModel)
                        showCustomerPage()
                    }
                    true
                }
                R.id.actionExit -> {
                    signOut()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Устанавливает в качестве выбрранного токена
     * */

    private fun setTokenModel(token: TokenModel) {
        selectedToken = token
    }

    /**
     * Переходит на страницу Клиента
     * */

    private fun showCustomerPage() {
        setFragmentResult(
            MainActivity.SHOW_CUSTOMER_FRAGMENT,
            bundleOf(MainActivity.BUNDLE_KEY to true)
        )
    }

    /**
     * Отключает кнопку предыдущая неделя
     * */

    private fun disablePrevBtn() {
        prevWeekBtn?.isEnabled = false
    }

    /**
     * Включает кнопку предыдущая неделя
     * */

    private fun enablePrevBtn() {
        prevWeekBtn?.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myExecutor.closeExecutor()
    }

    /**
     * Осуществляет загрузку, генерацию и установку талонов
     * */

    private fun workWithTokens(callBack: WeakReference<EnrollCallBack>) {
        val listTokenFirebaseModel =
            loadListToken(startWeek, endWeek) // уже существующие записи в БД
        val listTokenModel: List<TokenModel> = convertToTokenModel(listTokenFirebaseModel)

        val listNewTokenModel: List<TokenModel> = generateNewToken(
            listEmployee,
            listTokenModel,
            dayExecuteServices,
            hoursCompleteTemp
        ) // список новых талонов с указанием сотрудника

        Handler(Looper.getMainLooper()).post {
            callBack.get()?.setListEmployeeAndNewToken(listEmployee, listNewTokenModel)
        }
    }

    /**
     * Преобразует список TokenFirebaseModel в TokenModel
     * */

    private fun convertToTokenModel(listTokenFirebaseModel: List<TokenFirebaseModel>): List<TokenModel> {
        val listToken: MutableList<TokenModel> = mutableListOf()

        for (firebaseModel in listTokenFirebaseModel) {
            listToken.add(
                TokenModel(
                    startRecordDateTime = DateTimeHelper.convertToLocalDateTime(
                        firebaseModel.startRecordDateTime
                    ),
                    endRecordDateTime = DateTimeHelper.convertToLocalDateTime(
                        firebaseModel.endRecordDateTime
                    ),
                    idEmployee = firebaseModel.idEmployee
                )
            )
        }

        return listToken
    }

    /**
     * Добавляет в БД новую запись
     * @param token - талон записи
     * @param listServiceModel - список услуг оказываемых
     * */

    private fun registryNewTokenInDB(token: TokenModel, listServiceModel: List<ServiceModel>) {
        val myRef = reference.child(TOKEN_MODEL_FIREBASE_KEY) // database.getReference()

        val key = myRef.push().key
        key?.let {
            val tokenFirebase = TokenFirebaseModel(
                id = key,
                userId = getUserId(),
                branchId = branchId,
                carId = carId,
                startRecordDateTime = DateTimeHelper.convertToStringDateTime(token.startRecordDateTime),
                endRecordDateTime = DateTimeHelper.convertToStringDateTime(token.endRecordDateTime),
                idEmployee = token.idEmployee,
                hoursComplete = allHoursComplete.toLong(),
                price = price,
                listServices = listServiceModel
            )

            val childUpdates = hashMapOf<String, Any>(
                "/$key" to tokenFirebase
            )
            myRef.updateChildren(childUpdates)
        }
    }

    /**
     * Процесс регистрации
     * */

    private fun registration(callBack: WeakReference<EnrollCallBack>) {
        loadStartAndEndTime()

        listEmployee = loadListEmployee() // список сотрудников

        if (listEmployee.isNotEmpty()) {

            allHoursComplete = getHoursAllServices(listServiceModel)
            hoursCompleteTemp = allHoursComplete

            startWeek = updateStartWeekDateTime(startWeek)
            startWeekCash = startWeek
            endWeek = getDateTimeEndWeek()

            val startPrevTwoWeek = endWeek.minusWeeks(3L).plusDays(1L).withHour(timeStart.hour)
            val endPrevTwoWeek = endWeek.minusWeeks(1L)

            val listTokenPrevTwoWeek = loadListToken(startPrevTwoWeek, endPrevTwoWeek)
            listEmployee = sortListEmployee(listEmployee, listTokenPrevTwoWeek)

            // в отдельную функцию
            dayExecuteServices = hoursCompleteTemp / diffTimeWork
            hoursCompleteTemp %= diffTimeWork

            if (dayExecuteServices >= 7) {
                Handler(Looper.getMainLooper()).post {
                    callBack.get()?.showMessage(MESSAGE_CALL_CARSERVICE + phone)
                }
                return
            }

            if (!checkDate(hoursCompleteTemp, dayExecuteServices, startWeek, endWeek)) {
                updateStartEndWeekDateTime()
            }

            workWithTokens(callBack)
        } else {
            Handler(Looper.getMainLooper()).post {
                callBack.get()?.showMessage(MESSAGE_MAINTENANCE)
            }
        }
    }


    /**
     * Сортировка списка сотрудников на основе их загружености на протяжение двух последних недель
     * */

    private fun sortListEmployee(
        listEmployee: List<Employee>,
        listTokenPrevTwoWeek: List<TokenFirebaseModel>
    ): List<Employee> {
        val mapEmployee = mutableMapOf<String, Long>()
        val sortedListEmployee = mutableListOf<Employee>()
        for (employee in listEmployee) {
            mapEmployee[employee.id] = 0L
        }
        for (token in listTokenPrevTwoWeek) {
            if (mapEmployee.containsKey(token.idEmployee)){
                val value = mapEmployee[token.idEmployee]
                value?.let {
                    mapEmployee[token.idEmployee] = value + token.hoursComplete
                }
            }
        }
        val sortedMap = mapEmployee.toList().sortedBy { (key,value) -> value}.toMap()
        for (entry in sortedMap) {
            listEmployee.find { it.id == entry.key }?.let { sortedListEmployee.add(it) }
        }

        return sortedListEmployee
    }

    /**
     * Показывает titleCompleteTextView
     * */
    private fun visibleTitleComplete() {
        titleCompleteTextView?.visibility = View.VISIBLE
    }

    /**
     * Загружает время начала и окончания рабочего дня из БД
     * */

    private fun loadStartAndEndTime() {
    }

    /**
     * Показывает сообщение об отстсвие сотрудников
     * */
    override fun showMessage(message: String) {
        messageTextView?.visibility = View.VISIBLE
        progressBar?.visibility = View.GONE
        messageTextView?.text = message
    }

    /**
     * Фильтрует весь список новых талонов по id сотрудника
     * @param listNewToken - список талонов
     * @param idEmployee -
     * */
    private fun filterListTokenByIdEmployee(
        listNewToken: List<TokenModel>,
        idEmployee: String
    ): List<TokenModel> {
        val filtersList: MutableList<TokenModel> = mutableListOf()
        for (token in listNewToken) {
            if (token.idEmployee == idEmployee)
                filtersList.add(token)
        }
        return filtersList
    }

    /** Генерирует новые талоны на запись на основе существующих
     * @param listEmployee - список сотрудников
     * @param listTokenModel - список существующих талонов
     * @param dayExecuteServices - кол-во полных рабочих дней требуемых для выполнения услуг
     * @param hoursCompleteTemp - кол-во часов требуемых для выполнения услуг в последний рабочий день
     */

    private fun generateNewToken(
        listEmployee: List<Employee>,
        listTokenModel: List<TokenModel>,
        dayExecuteServices: Int,
        hoursCompleteTemp: Int
    ): List<TokenModel> {
        val listNewTokenModel: MutableList<TokenModel> = mutableListOf()

        for (employee in listEmployee) {
            val listSortedByIdEmployee: MutableList<TokenModel> = mutableListOf()
            for (token in listTokenModel) {
                if (token.idEmployee == employee.id) {
                    listSortedByIdEmployee.add(token)
                }
            }
            if (listSortedByIdEmployee.isEmpty()) {
                val listTokenTest = createTicket(
                    dayExecuteServices,
                    hoursCompleteTemp,
                    startWeek,
                    endWeek
                )
                listNewTokenModel.addAll(convertToListTokenModel(listTokenTest, employee.id))
            } else {
                var index = 0
                while (index <= listSortedByIdEmployee.size) {
                    var startDateTimeExecute: LocalDateTime
                    var endDateTimeExecute: LocalDateTime

                    when (index) {
                        0 -> {
                            startDateTimeExecute =
                                startWeek // время начала диапозона для генерации талон в createTicket
                            endDateTimeExecute = listSortedByIdEmployee[index].startRecordDateTime
                        }
                        (listSortedByIdEmployee.size) -> {
                            startDateTimeExecute =
                                listSortedByIdEmployee[index - 1].endRecordDateTime
                            endDateTimeExecute = endWeek
                        }
                        else -> {
                            startDateTimeExecute =
                                listSortedByIdEmployee[index - 1].endRecordDateTime
                            endDateTimeExecute =
                                listSortedByIdEmployee[index].startRecordDateTime
                        }
                    }
                    // список сгенерированных новых талонов
                    val listTokenTest = createTicket(
                        dayExecuteServices,
                        hoursCompleteTemp,
                        startDateTimeExecute,
                        endDateTimeExecute
                    )

                    listNewTokenModel.addAll(convertToListTokenModel(listTokenTest, employee.id))
                    index++
                }
            }
        }

        return listNewTokenModel
    }

    /**
     * Преобразует список TicketModelTest в список TokenModel
     * @param listTokenTest
     * @param employeeId - id сотрудника
     **/
    private fun convertToListTokenModel(
        listTokenTest: List<TicketModelTest>,
        employeeId: String
    ): Collection<TokenModel> {
        val listNewTokenModel: MutableList<TokenModel> = mutableListOf()
        for (token in listTokenTest) {
            listNewTokenModel.add(
                TokenModel(
                    startRecordDateTime = token.startRecordDateTime,
                    endRecordDateTime = token.endRecordDateTime,
                    idEmployee = employeeId
                )
            )
        }
        return listNewTokenModel
    }

    /**
     * Создает талона на запись
     * @param daysExecute кол-во полных рабочих дней необходимых для выполнение услуги
     * @param hoursExecute кол-во часов в псоледний рабочий дней необходимых на выполнение услуги
     * @param startWeek дата и время начало диапозона для генерации талонов
     * @param endWeek дата и время окончания диапозона
     * */
    private fun createTicket(
        daysExecute: Int,
        hoursExecute: Int,
        startWeek: LocalDateTime,
        endWeek: LocalDateTime
    ): List<TicketModelTest> {
        val listTicket: MutableList<TicketModelTest> = mutableListOf()
        val hoursDiff: Int = Duration.between(startWeek, endWeek).toHours().toInt() // неправильно
        var i = 0
        while (i < hoursDiff) {
            val daysDiff = i / diffTimeWork
            var startWeekTemp = startWeek.plusDays(daysDiff.toLong())
            var timeTemp = startWeekTemp.hour + i % diffTimeWork
            while (timeTemp >= timeEnd.hour) {
                startWeekTemp = startWeekTemp.plusDays(1)
                timeTemp -= timeEnd.hour
                timeTemp += timeStart.hour
            }
            startWeekTemp = startWeekTemp.withHour(timeTemp)
            if (startWeekTemp < endWeek) {
                var endWeekTemp = startWeekTemp.plusDays(daysExecute.toLong())
                timeTemp = endWeekTemp.hour + hoursExecute
                while (timeTemp > timeEnd.hour) {
                    endWeekTemp = endWeekTemp.plusDays(1)
                    timeTemp -= timeEnd.hour
                    timeTemp += timeStart.hour
                }
                endWeekTemp = endWeekTemp.withHour(timeTemp)
                if (endWeekTemp <= endWeek) {
                    listTicket.add(TicketModelTest(startWeekTemp, endWeekTemp))
                } else {
                    break
                }
            } else {
                break
            }
            i++
        }
        return listTicket
    }

    /**
     * Загружает список записей из БД для текущей недели
     * */

    private fun loadListToken(
        startWeekInterval: LocalDateTime,
        endWeekInterval: LocalDateTime
    ): List<TokenFirebaseModel> {
        val listToken: MutableList<TokenFirebaseModel> = mutableListOf()

        val tokenQuery =
            reference.child(TOKEN_MODEL_FIREBASE_KEY).orderByChild("startRecordDateTime")
                .startAt(DateTimeHelper.convertToStringDateTime(startWeekInterval))
                .endAt(DateTimeHelper.convertToStringDateTime(endWeekInterval))

        var allCount = 0L
        var count = 1L

        tokenQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (data in snapshot.children) {
                    val tokenFirebase = data.getValue<TokenFirebaseModel>()
                    tokenFirebase?.apply {
                        listToken.add(tokenFirebase)
                        count++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(FIREBASE_LOG_TAG, error.message)
            }
        })

        val tokenDataSnapshot = tokenQuery.get()
        while (count < allCount || !tokenDataSnapshot.isComplete) {
            Thread.sleep(500)
        }

        return listToken
    }

    /**
     * Загружает список сотрудников
     * */

    private fun loadListEmployee(): List<Employee> {
        val childName = getString(R.string.employees_firebase_key)

        val lisEmployee: MutableList<Employee> = mutableListOf()

        var allCount = 0L
        var count = 1L

        val query =
            reference.child(childName).orderByChild("branchId").equalTo(branchId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (child in snapshot.children) {
                    val employee = child.getValue<Employee>()
                    employee?.let {
                        if (it.category == nameCategory) lisEmployee.add(it)
                    }
                    count++
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })

        val dataSnapshot = query.get()
        while (count < allCount || !dataSnapshot.isComplete) {
            Thread.sleep(500)
        }

        return lisEmployee
    }

    /**
     * Увеличивыет значения даты и времени начала и окончанияе недели на одну неделю
     **/

    private fun updateStartEndWeekDateTime() {
        startWeek = endWeek.plusDays(1).withHour(timeStart.hour).withMinute(timeStart.minute)
        endWeek = endWeek.plusDays(7).withHour(timeEnd.hour).withMinute(timeEnd.minute)
    }

    /**
     * Изменяет дату и время начала и конца неделю на предыдущую
     * */
    private fun minusWeekStartAndEndDateTime() {
        startWeek = startWeek.minusWeeks(1)
        if (startWeek <= startWeekCash) {
            startWeek = startWeekCash
            disablePrevBtn() // Дальше уже неделя прошла
        }
        endWeek = endWeek.minusWeeks(1)
    }

    /**
     * Проверят диапазон даты и времеми на возможность сгенерировать талоны
     * @param dayExecuteServices кол-во полных рабочих дней необходимых для выполнение услуги
     * @param hoursCompleteTemp кол-во часов в псоледний рабочий дней необходимых на выполнение услуги
     * @param startWeek дата и время начало диапозона для генерации талонов
     * @param endWeek дата и время окончания диапозона
     * */

    private fun checkDate(
        hoursCompleteTemp: Int,
        dayExecuteServices: Int,
        startWeek: LocalDateTime,
        endWeek: LocalDateTime
    ): Boolean {
        val hours: Int = endWeek.hour - startWeek.hour
        val days: Int = Period.between(startWeek.toLocalDate(), endWeek.toLocalDate()).days
        return days > dayExecuteServices || (days == dayExecuteServices && hours > hoursCompleteTemp)
    }

    /**
     * Вычисляет начало неделя для записи
     * */

    private fun updateStartWeekDateTime(startWeek: LocalDateTime): LocalDateTime {
        var resultDateTime = startWeek.plusHours(1).withMinute(0).withSecond(0).withNano(0)
        if (resultDateTime.toLocalTime() < timeStart) {
            resultDateTime = resultDateTime.withHour(timeStart.hour).withMinute(timeStart.minute)
        }
        if (resultDateTime.toLocalTime() > timeEnd) {
            resultDateTime =
                resultDateTime.plusDays(1).withHour(timeStart.hour).withMinute(timeStart.minute)
        }
        return resultDateTime
    }

    /**
     * Вычисляет окончание неделя для записи
     * */

    private fun getDateTimeEndWeek(): LocalDateTime {
        return LocalDateTime.now().let { // apply
            val diffDayOfWeek = 6 - it.dayOfWeek.ordinal
            it.plusDays(diffDayOfWeek.toLong()).withHour(timeEnd.hour).withMinute(timeEnd.minute)
                .withSecond(0).withNano(0)
        }
    }

    /**
     * Вычисляет общее время на выполнение услуг
     * */

    private fun getHoursAllServices(listServiceModel: List<ServiceModel>): Int {
        var countHours = 0
        for (service in listServiceModel) {
            countHours += service.hours
        }
        return countHours
    }

    companion object {
        private const val LIST_SERVICE = "list_services"
        private const val BRANCH = "branch"
        private const val CAR_ID = "car_id"
        private const val PRICE = "price"
        private const val NAME_CATEGORY = "name_category"
        private const val FIREBASE_LOG_TAG = "Firebase"

        private const val TOKEN_MODEL_FIREBASE_KEY = "tickets"

        private const val TITLE_TOOLBAR = "Запись"

        private const val MESSAGE_MAINTENANCE =
            "Ведуться технические работы, попробуйте в другой раз снова"
        private const val MESSAGE_CALL_CARSERVICE =
            "Время выполнения услуги превышает 7 дней, позвоните в автосервис для записи:"

        fun newInstance(
            list: List<ServiceModel>,
            branchModel: BranchModel,
            carId: String,
            price: Long,
            categoryName: String
        ): SelectDateTimeFragment {
            return SelectDateTimeFragment().apply {
                arguments = bundleOf(
                    LIST_SERVICE to list,
                    BRANCH to branchModel,
                    CAR_ID to carId,
                    PRICE to price,
                    NAME_CATEGORY to categoryName
                )
            }
        }
    }

    /**
     * Устанавливает значения во View
     * */

    override fun setListEmployeeAndNewToken(
        listEmployee: List<Employee>,
        listNewToken: List<TokenModel>
    ) {
        if (listNewToken.isEmpty()) {
            updateStartEndWeekDateTime()
            myExecutor.executeOnNextWeek()
            startWeekCash = startWeek
        } else {
            showViews()

            visibleTitleComplete()
            timeCompleteTextView?.text =
                if (dayExecuteServices == 0) "$hoursCompleteTemp ч." else "$dayExecuteServices д. $hoursCompleteTemp ч."

            // список талонов только для одного сотрудника
            var listNewTokenFilterByIdEmployee =
                filterListTokenByIdEmployee(listNewToken, listEmployee[0].id)

            val listEmployeeSpinnerAdapter: ArrayAdapter<Employee> =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listEmployee)
            listEmployeeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            listEmployeeSpinner?.adapter = listEmployeeSpinnerAdapter

            listTokenRecyclerAdapter?.setDataSet(listNewTokenFilterByIdEmployee)

            listEmployeeSpinner?.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        itemSelected: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedEmployee = parent?.getItemAtPosition(position) as? Employee
                        selectedEmployee?.let {
                            listNewTokenFilterByIdEmployee =
                                filterListTokenByIdEmployee(listNewToken, it.id)
                        }
                        listTokenRecyclerAdapter?.setDataSet(listNewTokenFilterByIdEmployee)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("SELECTED", "Ничего не выбранно")
                    }
                }
        }
    }

    /**
     * Показать все виджеты
     * */

    private fun showViews() {
        progressBar?.visibility = View.GONE
        nextWeekBtn?.visibility = View.VISIBLE
        prevWeekBtn?.visibility = View.VISIBLE
        listEmployeeSpinner?.visibility = View.VISIBLE
        titleSelectEmployeeTextView?.visibility = View.VISIBLE
        titleSelectDateTimeTextView?.visibility = View.VISIBLE
        listTokenRecyclerView?.visibility = View.VISIBLE
    }

    /**
     * Скрывать все виджеты
     * */

    private fun goneViews() {
        progressBar?.visibility = View.VISIBLE
        nextWeekBtn?.visibility = View.GONE
        prevWeekBtn?.visibility = View.GONE
        listEmployeeSpinner?.visibility = View.GONE
        titleSelectEmployeeTextView?.visibility = View.GONE
        titleSelectDateTimeTextView?.visibility = View.GONE
        listTokenRecyclerView?.visibility = View.GONE
    }

    inner class EnrollExecutor(callBack: EnrollCallBack) {
        private val callBack: WeakReference<EnrollCallBack> = WeakReference(callBack)
        private val executor = Executors.newSingleThreadExecutor()

        fun executeOnThisWeek() {
            executor.execute {
                registration(callBack)
            }
        }

        fun executeOnNextWeek() {
            executor.execute {
                workWithTokens(callBack)
            }
        }

        fun closeExecutor() {
            executor.shutdownNow()
        }
    }
}
