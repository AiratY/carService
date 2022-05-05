package ru.airatyunusov.carservice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import ru.airatyunusov.carservice.callbacks.EnrollCallBack
import ru.airatyunusov.carservice.model.*
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.util.concurrent.Executors

class SelectDateTimeFragment : BlankFragment(), EnrollCallBack {

    private var dateTimeSpinner: Spinner? = null
    private var listEmployeeSpinner: Spinner? = null
    private var enrollButton: Button? = null
    private var nextWeekBtn: Button? = null
    private var prevWeekBtn: Button? = null
    private var progressBar: ProgressBar? = null

    private val timeStart: LocalTime = LocalTime.of(8, 0)
    private val timeEnd: LocalTime = LocalTime.of(20, 0)
    private val diffTimeWork: Int = timeEnd.hour - timeStart.hour
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

    private var carId = ""
    private var branchId = ""
    private var price = 0L

    private val database =
        Firebase.database("https://carservice-93ef9-default-rtdb.europe-west1.firebasedatabase.app/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_date_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateTimeSpinner = view.findViewById(R.id.dateTimeSpinner)
        listEmployeeSpinner = view.findViewById(R.id.listEmployeeSpinner)
        enrollButton = view.findViewById(R.id.enrollButton)
        nextWeekBtn = view.findViewById(R.id.nextWeekButton)
        prevWeekBtn = view.findViewById(R.id.prevWeekButton)
        progressBar = view.findViewById(R.id.progressBar)

        goneViews()

        /**
         * Нужно учитывать выходные у сотрудников и праздничные дни
         * Сделать минмальную единицу 30 мин.
         */

        arguments?.let {
            listServiceModel = it.get(LIST_SERVICE) as? List<ServiceModel> ?: emptyList()
            carId = it.getString(CAR_ID, "")
            branchId = it.getString(BRANCH_ID, "")
            price = it.getLong(PRICE, 0)

            disablePrevBtn()
            // myExecutor = EnrollExecutor(this)
            myExecutor.executeOnThisWeek()

            enrollButton?.setOnClickListener {
                selectedToken?.let { token ->
                    registryNewTokenInDB(token, listServiceModel)
                    showCustomerPage()
                }
            }

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
    }

    /**
     * Переходит на страницу КЛиента
     * */

    private fun showCustomerPage() {
        setFragmentResult(
            MainActivity.SHOW_CUSTOMER_FRAGMENT,
            bundleOf(MainActivity.BUNDLE_KEY to true)
        )
    }

    private fun disablePrevBtn() {
        prevWeekBtn?.isEnabled = false
    }

    private fun enablePrevBtn() {
        prevWeekBtn?.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myExecutor.closeExecutor()
    }

    private fun workWithTokens(callBack: WeakReference<EnrollCallBack>) {
        val listTokenModel: List<TokenModel> = loadListToken() // уже существующие записи в БД

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
     * Добавляет в БД новую запись
     * @param token - талон записи
     * @param listServiceModel - список услуг оказываемых
     * */

    private fun registryNewTokenInDB(token: TokenModel, listServiceModel: List<ServiceModel>) {
        val myRef = database.getReference(TOKEN_MODEL_FIREBASE_KEY)

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
                price = price,
                listServices = listServiceModel
            )

            val childUpdates = hashMapOf<String, Any>(
                "/$key" to tokenFirebase
            )
            myRef.updateChildren(childUpdates)
        }
    }


    private fun registration(callBack: WeakReference<EnrollCallBack>) {
        listEmployee = loadListEmployee() // список сотрудников

        if (listEmployee.isNotEmpty()) {
            hoursCompleteTemp = getHoursAllServices(listServiceModel)

            startWeek = updateStartWeekDateTime(startWeek)
            startWeekCash = startWeek
            endWeek = getDateTimeEndWeek()

            // в отдельную функцию
            dayExecuteServices = hoursCompleteTemp / diffTimeWork
            hoursCompleteTemp %= diffTimeWork

            if (!checkDate(hoursCompleteTemp, dayExecuteServices, startWeek, endWeek)) {
                updateStartEndWeekDateTime()
            }

            /*updateStartEndWeekDateTime()
            updateStartEndWeekDateTime()*/

            workWithTokens(callBack)
        } else {
            showMessage()
        }
    }

    /**
     * Показывает сообщение об отстсвие сотрудников
     * */
    private fun showMessage() {
        TODO("Not yet implemented")
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

    // createToken
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

    private fun loadListToken(): List<TokenModel> {
        val listToken: MutableList<TokenModel> = mutableListOf()

        val tokenQuery =
            database.reference.child(TOKEN_MODEL_FIREBASE_KEY).orderByChild("startRecordDateTime")
                .startAt(DateTimeHelper.convertToStringDateTime(startWeek))
                .endAt(DateTimeHelper.convertToStringDateTime(endWeek))

        var allCount = 0L
        var count = 1L

        tokenQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (data in snapshot.children) {
                    val tokenFirebase = data.getValue<TokenFirebaseModel>()
                    tokenFirebase?.apply {
                        val token = TokenModel(
                            startRecordDateTime = DateTimeHelper.convertToLocalDateTime(
                                startRecordDateTime
                            ),
                            endRecordDateTime = DateTimeHelper.convertToLocalDateTime(
                                endRecordDateTime
                            ),
                            idEmployee = idEmployee
                        )
                        listToken.add(token)
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

    private fun loadListEmployee(): List<Employee> {
        val childName = getString(R.string.employees_firebase_key)

        val lisEmployee: MutableList<Employee> = mutableListOf()

        var allCount = 0L
        var count = 1L

        val query =
            database.reference.child(childName).orderByChild("branchId").equalTo(branchId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (child in snapshot.children) {
                    val employee = child.getValue<Employee>()
                    employee?.let { lisEmployee.add(it) }
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

    /*private fun loadListEmployee(): List<Employee> {
        val list: MutableList<Employee> = mutableListOf()
        val employeesRef = database.getReference(EMPLOYEES_FIREBASE_KEY)
        val taskDataSnapshot = employeesRef.get()

        while (!taskDataSnapshot.isComplete) {
            Thread.sleep(500)
        }
        val hashMap = taskDataSnapshot.result.getValue<HashMap<String, Employee>>()
        if (hashMap != null) {
            for (hash in hashMap) {
                list.add(hash.value)
            }
        }

        return list
    }*/

    private fun updateStartEndWeekDateTime() {
        startWeek = endWeek.plusDays(1).withHour(timeStart.hour).withMinute(timeStart.minute)
        endWeek = endWeek.plusDays(7).withHour(timeEnd.hour).withMinute(timeEnd.minute)
    }

    /**
     * Изменяет дату и время начала и конца неделю на предыдущую
     * */
    private fun minusWeekStartAndEndDateTime() {
        startWeek = startWeek.minusWeeks(1)
        if (startWeek < startWeekCash) {
            startWeek = startWeekCash
            disablePrevBtn() // Дальше уже неделя прошла
        }
        endWeek = endWeek.minusWeeks(1)
    }

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

    private fun getDateTimeEndWeek(): LocalDateTime {
        return LocalDateTime.now().let { // apply
            val diffDayOfWeek = 6 - it.dayOfWeek.ordinal
            it.plusDays(diffDayOfWeek.toLong()).withHour(timeEnd.hour).withMinute(timeEnd.minute)
                .withSecond(0).withNano(0)
        }
    }

    private fun getHoursAllServices(listServiceModel: List<ServiceModel>): Int {
        var countHours = 0
        for (service in listServiceModel) {
            countHours += service.hours
        }
        return countHours
    }

    companion object {
        private const val LIST_SERVICE = "list_services"
        private const val BRANCH_ID = "branch_id"
        private const val CAR_ID = "car_id"
        private const val PRICE = "price"
        private const val FIREBASE_LOG_TAG = "Firebase"

        private const val TOKEN_MODEL_FIREBASE_KEY = "tickets"
        private const val EMPLOYEES_FIREBASE_KEY = "employees"

        fun newInstance(
            list: List<ServiceModel>,
            branchId: String,
            carId: String,
            price: Long
        ): SelectDateTimeFragment {
            return SelectDateTimeFragment().apply {
                arguments = bundleOf(
                    LIST_SERVICE to list,
                    BRANCH_ID to branchId,
                    CAR_ID to carId,
                    PRICE to price
                )
            }
        }
    }

    override fun setListEmployeeAndNewToken(
        listEmployee: List<Employee>,
        listNewToken: List<TokenModel>
    ) {
        showViews()

        // список талонов только для одного сотрудника
        var listNewTokenFilterByIdEmployee =
            filterListTokenByIdEmployee(listNewToken, listEmployee[0].id)

        val listEmployeeSpinnerAdapter: ArrayAdapter<Employee> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listEmployee)
        listEmployeeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        listEmployeeSpinner?.adapter = listEmployeeSpinnerAdapter

        // Заполняем спинор талонами
        val spinnerAdapter: ArrayAdapter<TokenModel> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listNewTokenFilterByIdEmployee
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        dateTimeSpinner?.adapter = spinnerAdapter

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
                    spinnerAdapter.clear()
                    spinnerAdapter.addAll(listNewTokenFilterByIdEmployee)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

        dateTimeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View?,
                position: Int,
                id: Long
            ) {
                selectedToken = parent?.getItemAtPosition(position) as? TokenModel
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun showViews() {
        progressBar?.visibility = View.GONE
        nextWeekBtn?.visibility = View.VISIBLE
        prevWeekBtn?.visibility = View.VISIBLE
        dateTimeSpinner?.visibility = View.VISIBLE
        listEmployeeSpinner?.visibility = View.VISIBLE
        enrollButton?.visibility = View.VISIBLE
    }

    private fun goneViews() {
        progressBar?.visibility = View.VISIBLE
        nextWeekBtn?.visibility = View.GONE
        prevWeekBtn?.visibility = View.GONE
        dateTimeSpinner?.visibility = View.GONE
        listEmployeeSpinner?.visibility = View.GONE
        enrollButton?.visibility = View.GONE
    }

    inner class EnrollExecutor(callBack: EnrollCallBack) {
        private val callBack: WeakReference<EnrollCallBack> = WeakReference(callBack)
        private val executor = Executors.newSingleThreadExecutor()

        fun executeOnThisWeek() {
            executor.execute {
                Thread.sleep(2000)
                registration(callBack)
            }
        }

        fun executeOnNextWeek() {
            executor.execute {
                Thread.sleep(2000)
                workWithTokens(callBack)
            }
        }

        fun closeExecutor() {
            executor.shutdownNow()
        }
    }
}
