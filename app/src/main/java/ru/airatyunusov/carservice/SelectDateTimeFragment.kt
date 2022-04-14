package ru.airatyunusov.carservice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.ServiceModel
import ru.airatyunusov.carservice.model.TicketModelTest
import ru.airatyunusov.carservice.model.TokenModel
import java.time.*

class SelectDateTimeFragment : Fragment() {

    private var dateTimeSpinner: Spinner? = null
    private var listEmployeeSpinner: Spinner? = null

    private val timeStart: LocalTime = LocalTime.of(8, 0)
    private val timeEnd: LocalTime = LocalTime.of(20, 0)
    private val diffTimeWork: Int = timeEnd.hour - timeStart.hour
    private var listServiceModel: List<ServiceModel> = emptyList()

    private val m: Int = 0//????

    private var startWeek: LocalDateTime = LocalDateTime.now()
    private var endWeek: LocalDateTime = LocalDateTime.now()

    private var selectedEmployee: Employee? = null
    private var selectedToken: TokenModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_date_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateTimeSpinner = view.findViewById(R.id.dateTimeSpinner)
        listEmployeeSpinner = view.findViewById(R.id.listEmployeeSpinner)
        val enrollButton: Button = view.findViewById(R.id.enrollButton)

        /**
         * Нужно учитывать выходные у сотрудников и праздничные дни
         * Сделать минмальную единицу 30 мин.
         **/

        arguments?.let {
            listServiceModel = it.get(LIST_SERVICE) as? List<ServiceModel> ?: emptyList()

            //val countHours = getHoursAllServices(listServiceModel)
            //getListEnrollForServices(countHours)

            registration()

            enrollButton.setOnClickListener {
                selectedToken?.let { token ->
                    registryNewTokenInDB(token, listServiceModel)
                }
            }
        }
    }

    /**
     * Добавляет в БД новую запись
     * @param token - талон записи
     * @param listServiceModel - список услуг оказываемых
     * */

    private fun registryNewTokenInDB(token: TokenModel, listServiceModel: List<ServiceModel>) {
        TODO("Not yet implemented")
    }

    private fun registration() {
        //if (m < listServiceModel.size) {
        /*var hoursCompleteTemp: Int = 0//Время выполнения текущей услуги
        for (serviceModel in listServiceModel) {
            hoursCompleteTemp += serviceModel.hours
        }*/

        var hoursCompleteTemp = getHoursAllServices(listServiceModel)


        startWeek = updateStartWeekDateTime(startWeek)
        endWeek = getDateTimeEndWeek()

        //в отдельную функцию
        val dayExecuteServices = hoursCompleteTemp / diffTimeWork
        hoursCompleteTemp %= diffTimeWork

        if (checkDate(hoursCompleteTemp, dayExecuteServices, startWeek, endWeek)) {
            print(1)
        } else {
            updateStartEndWeekDateTime()
        }
        val listEmployee: List<Employee> = loadListEmployee()//список сотрудников
        val listTokenModel: List<TokenModel> = loadListToken() //уже существующие записи в БД

        var listNewToken: List<TicketModelTest> = emptyList() //список новы талонов на запись
        var listNewTokenModel: List<TokenModel> =
            emptyList() //список новых талонов с указанием сотрудника

        if (listTokenModel.isEmpty()) {
            listNewToken =
                createTicket(dayExecuteServices, hoursCompleteTemp, startWeek, endWeek)
        } else {
            listNewTokenModel = generateNewToken(
                listEmployee,
                listTokenModel,
                dayExecuteServices,
                hoursCompleteTemp
            )
        }
        //список талонов только для одного сотрудника
        var listNewTokenFilterByIdEmployee =
            filterListTokenByIdEmployee(listNewTokenModel, listEmployee[0].id)

        val listEmployeeSpinnerAdapter: ArrayAdapter<Employee> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listEmployee)
        listEmployeeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        listEmployeeSpinner?.adapter = listEmployeeSpinnerAdapter


        //Заполняем спинор талонами
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
                            filterListTokenByIdEmployee(listNewTokenModel, it.id)

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

        /*} else {
            //Нет, конец
        }*/
    }

    /**
     * Фильтрует весь список новых талонов по id сотрудника
     * @param listNewToken - список талонов
     * @param idEmployee -
     * */
    private fun filterListTokenByIdEmployee(
        listNewToken: List<TokenModel>,
        idEmployee: Int
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
            var listSortedByIdEmployee: MutableList<TokenModel> = mutableListOf()
            for (token in listTokenModel) {
                if (token.idEmployee == employee.id) {
                    listSortedByIdEmployee.add(token)
                }
            }
            var index = 0
            while (index <= listSortedByIdEmployee.size) {
                var startDateTimeExecute: LocalDateTime
                var endDateTimeExecute: LocalDateTime

                when (index) {
                    0 -> {
                        startDateTimeExecute =
                            startWeek //время начала диапозона для генерации талон в createTicket
                        endDateTimeExecute = listSortedByIdEmployee[index].startRecordDateTime
                    }
                    (listSortedByIdEmployee.size) -> {
                        startDateTimeExecute = listSortedByIdEmployee[index - 1].endRecordDateTime
                        endDateTimeExecute = endWeek
                    }
                    else -> {
                        startDateTimeExecute = listSortedByIdEmployee[index].endRecordDateTime
                        endDateTimeExecute = listSortedByIdEmployee[index + 1].startRecordDateTime
                    }
                }
                //список сгенерированных новых талонов
                val listTokenTest = createTicket(
                    dayExecuteServices,
                    hoursCompleteTemp,
                    startDateTimeExecute,
                    endDateTimeExecute
                )
                for (token in listTokenTest) {
                    listNewTokenModel.add(
                        TokenModel(
                            token.startRecordDateTime,
                            token.endRecordDateTime,
                            employee.id
                        )
                    )
                }
                index++
            }
        }
        return listNewTokenModel
    }

    //createToken
    private fun createTicket(
        daysExecute: Int,
        hoursExecute: Int,
        startWeek: LocalDateTime,
        endWeek: LocalDateTime
    ): List<TicketModelTest> {
        val listTicket: MutableList<TicketModelTest> = mutableListOf()
        val hoursDiff: Int = Duration.between(startWeek, endWeek).toHours().toInt()//неправильно
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
        return listOf(
            TokenModel(
                LocalDateTime.of(2022, 4, 13, 14, 0),
                LocalDateTime.of(2022, 4, 14, 17, 0),
                1
            ),
            TokenModel(
                LocalDateTime.of(2022, 4, 12, 18, 0),
                LocalDateTime.of(2022, 4, 13, 17, 0),
                2
            ),
            TokenModel(
                LocalDateTime.of(2022, 4, 15, 14, 0),
                LocalDateTime.of(2022, 4, 15, 17, 0),
                3
            ),
        )
        //return emptyList()
    }

    private fun loadListEmployee(): List<Employee> {
        return listOf(
            Employee(1, "Иван", "Иванов", "Иванович"),
            Employee(2, "Петр", "Иванов", "Иванович"),
            Employee(3, "Егор", "Иванов", "Иванович"),
        )
    }

    private fun updateStartEndWeekDateTime() {
        startWeek = endWeek.plusDays(1).withHour(timeStart.hour).withMinute(timeStart.minute)
        endWeek = endWeek.plusDays(7).withHour(timeEnd.hour).withMinute(timeEnd.minute)
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
        /* var nowDateTime = LocalDateTime.now()
         val diffDayOfWeek = 6 - nowDateTime.dayOfWeek.ordinal
         nowDateTime = nowDateTime.plusDays(diffDayOfWeek.toLong())
         nowDateTime = nowDateTime.withHour(timeEnd.hour)*/

        return LocalDateTime.now().let {//apply
            val diffDayOfWeek = 6 - it.dayOfWeek.ordinal
            it.plusDays(diffDayOfWeek.toLong()).withHour(timeEnd.hour).withMinute(timeEnd.minute)
                .withSecond(0).withNano(0)
        }
    }

    /*
    * Функция запроса данных с БД
    * Обработка этих данных
    * Выдача талонов
    *
    */
    private fun getListEnrollForServices(countHours: Int) {

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
        private const val BRANCH_SERVICES = "branch_services"
        private const val CAR = "car"

        fun newInstance(list: List<ServiceModel>): SelectDateTimeFragment {
            return SelectDateTimeFragment().apply {
                arguments = bundleOf(LIST_SERVICE to list)
            }
        }
    }
}