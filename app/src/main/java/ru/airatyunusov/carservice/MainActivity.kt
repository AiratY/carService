package ru.airatyunusov.carservice

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.airatyunusov.carservice.admin.AdminFragment
import ru.airatyunusov.carservice.admin.ListTokenAdminFragment
import ru.airatyunusov.carservice.admin.branchs.BranchFragment
import ru.airatyunusov.carservice.admin.branchs.DetailBranchFragment
import ru.airatyunusov.carservice.admin.employees.EmployeeFragment
import ru.airatyunusov.carservice.admin.services.CatalogServicesFragment
import ru.airatyunusov.carservice.admin.services.CategoryServicesFragment
import ru.airatyunusov.carservice.admin.services.ServicesFragment
import ru.airatyunusov.carservice.auth.AuthorizationFragment
import ru.airatyunusov.carservice.customer.CustomerFragment
import ru.airatyunusov.carservice.customer.cars.CarDetailFragment
import ru.airatyunusov.carservice.customer.enroll.EnrollFragment
import ru.airatyunusov.carservice.customer.enroll.SelectDateTimeFragment
import ru.airatyunusov.carservice.customer.services.ListTokenCustomerFragment
import ru.airatyunusov.carservice.employee.EmployeePageFragment
import ru.airatyunusov.carservice.model.*
import ru.airatyunusov.carservice.token.TokenDetailFragment

class MainActivity : AppCompatActivity() {
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.adminBottomNavigationView)

        val sharedPreferences = getSharedPreferences(
            getString(R.string.user_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        if (!sharedPreferences.getBoolean(AUTH, false)) {
            showAuthorizationFragment()
        } else {
            val ROLE_KEY = getString(R.string.ROLE_SHARED_PREFERENCE_KEY)
            val role = sharedPreferences.getString(ROLE_KEY, "")
            when (role) {
                ROLE_CUSTOMER -> {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<CustomerFragment>(R.id.fragment_container_view)
                        addToBackStack(NAME_BACK_STACK)
                    }
                    setCustomerListener()
                }
                ROLE_EMPLOYEE -> {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<EmployeePageFragment>(R.id.fragment_container_view)
                        addToBackStack(NAME_BACK_STACK)
                    }
                }
                ROLE_ADMIN -> {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<AdminFragment>(R.id.fragment_container_view)
                        addToBackStack(NAME_BACK_STACK)
                    }
                    setAdminListener()
                }
                else -> {
                    showAuthorizationFragment()
                }
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_ENROLL, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val listCars = bundle.get(LIST_CARS) as? List<CarModel> ?: emptyList()
                replaceFragment(EnrollFragment.newInstance(listCars))
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_SELECT_DATE_TIME, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val listServicesModel: List<ServiceModel> =
                    bundle.get(LIST_SERVICES) as? List<ServiceModel> ?: emptyList()
                val branchId = bundle.get(BRANCH) as? BranchModel ?: BranchModel()
                val carID = bundle.getString(CAR_ID, "")
                val price = bundle.getLong(PRICE, 0)
                val nameCategory = bundle.getString(CATEGORY, "")
                replaceFragment(
                    SelectDateTimeFragment.newInstance(
                        listServicesModel,
                        branchId,
                        carID,
                        price,
                        nameCategory
                    )
                )
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_ADMIN_FRAGMENT, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                replaceFragment(AdminFragment())
                setAdminListener()
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_DETAIL_BRANCH, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val branchModel = bundle.get(BRANCH) as? BranchModel
                if (branchModel == null) {
                    replaceFragment(DetailBranchFragment())
                } else {
                    replaceFragment(DetailBranchFragment.newInstance(branchModel))
                }
            }
        }
        supportFragmentManager.setFragmentResultListener(SHOW_EMPLOYEE, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val branchId = bundle.getString(BRANCH_ID) ?: ""
                val employee = bundle.get(EMPLOYEE) as? Employee
                if (employee != null) {
                    replaceFragment(EmployeeFragment.newInstance(employee))
                } else {
                    replaceFragment(EmployeeFragment.newInstance(branchId))
                }
            }
        }
        supportFragmentManager.setFragmentResultListener(SHOW_BRANCH, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val branch: BranchModel = bundle.get(BRANCH_ITEM) as? BranchModel ?: BranchModel()
                /*val branchId = bundle.getString(BRANCH_ID) ?: ""*/
                replaceFragment(BranchFragment.newInstance(branch))
            }
        }
        supportFragmentManager.setFragmentResultListener(SHOW_CATALOG_SERVICES, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                replaceFragment(CatalogServicesFragment())
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_DETAIL_CAR, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val car = bundle.get(CAR) as? CarModel
                if (car == null) {
                    replaceFragment(CarDetailFragment())
                } else {
                    replaceFragment(CarDetailFragment.newInstance(car))
                }
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_DETAIL_TOKEN, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val token = bundle.get(TOKEN) as? TokenFirebaseModel ?: TokenFirebaseModel()
                val isDelete = bundle.getBoolean(IS_DELETE_TOKEN)
                replaceFragment(TokenDetailFragment.newInstance(token, isDelete))
            }
        }

        supportFragmentManager.setFragmentResultListener(
            SHOW_CUSTOMER_FRAGMENT,
            this
        ) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                replaceFragment(CustomerFragment())
                setCustomerListener()
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_AUTH, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                replaceFragment(AuthorizationFragment())
                // showAuthorizationFragment()
            }
        }

        supportFragmentManager.setFragmentResultListener(
            SHOW_EMPLOYEE_FRAGMENT,
            this
        ) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                replaceFragment(EmployeePageFragment())
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_ADD_SERVICE, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val serviceModel = bundle.get(SERVICE) as? ServiceModel
                if (serviceModel == null) {
                    replaceFragment(ServicesFragment())
                } else {
                    replaceFragment(ServicesFragment.newInstance(serviceModel))
                }
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_ADD_CATEGORY_SERVICE, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val categoryServices = bundle.get(CATEGORY) as? CategoryServices
                if (categoryServices == null) {
                    replaceFragment(CategoryServicesFragment())
                } else {
                    replaceFragment(CategoryServicesFragment.newInstance(categoryServices))
                }
            }
        }
    }

    private fun showAuthorizationFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<AuthorizationFragment>(R.id.fragment_container_view)
            addToBackStack(NAME_BACK_STACK)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin_page, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.actionExit -> {
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val topFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
        topFragment?.let { checkFragmentInBottomBar(it) }
    }

    /**
     * Проверяет фрагмент на необходимость BottomNavigationView
     * */
    private fun checkFragmentInBottomBar(fragment: Fragment) {
        if ((fragment as? BaseFragment)?.isShowBottomNavigationView() == true) {
            visibleBottomNavigationView()
        } else {
            goneBottomNavigationView()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        checkFragmentInBottomBar(fragment)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, fragment)
            addToBackStack(NAME_BACK_STACK)
        }
    }

    /**
     * Показывает BottomNavigationView
     * */
    private fun visibleBottomNavigationView() {
        bottomNavigationView?.visibility = View.VISIBLE
    }

    /**
     * Скрывает BottomNavigationView
     * */
    private fun goneBottomNavigationView() {
        bottomNavigationView?.visibility = View.GONE
    }

    private fun setCustomerListener() {

        bottomNavigationView?.menu?.findItem(R.id.listServices)?.isVisible = false
        // User

        bottomNavigationView?.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.profile -> {
                    replaceFragment(CustomerFragment())
                    true
                }
                R.id.listToken -> {
                    replaceFragment(ListTokenCustomerFragment())
                    true
                }
                else -> false
            }
        }
        visibleBottomNavigationView()
    }

    private fun setAdminListener() {

        bottomNavigationView?.menu?.findItem(R.id.listServices)?.isVisible = true
        // ADMIN
        bottomNavigationView?.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.listServices -> {
                    replaceFragment(CatalogServicesFragment())
                    true
                }
                R.id.profile -> {
                    replaceFragment(AdminFragment())
                    true
                }
                R.id.listToken -> {
                    replaceFragment(ListTokenAdminFragment())
                    true
                }
                else -> false
            }
        }

        visibleBottomNavigationView()
    }

    companion object {
        private const val NAME_BACK_STACK = "fragments"
        const val MESSAGE_USER = "action"
        const val SHOW_ENROLL = "show enroll"
        const val SHOW_ADMIN_FRAGMENT = "show admin"
        const val SHOW_CUSTOMER_FRAGMENT = "show_customer_page"
        const val SHOW_EMPLOYEE_FRAGMENT = "show_employee_page"
        const val SHOW_SELECT_DATE_TIME = "show_select_date_time"
        const val SHOW_ADD_SERVICE = "show_ADD_SERVICE"
        const val SHOW_ADD_CATEGORY_SERVICE = "show_ADD_category_SERVICE"
        const val BUNDLE_KEY = "show"
        const val SHOW_DETAIL_BRANCH = "show_detail_branch"
        const val SHOW_DETAIL_TOKEN = "show_detail_token"
        const val SHOW_BRANCH = "show_branch"
        const val SHOW_DETAIL_CAR = "show_detail_car"
        const val CAR = "car"
        const val SHOW_CATALOG_SERVICES = "show_catalog"
        const val BRANCH_ITEM = "branch_item"
        const val BRANCH_ID = "branch_id"
        const val CAR_ID = "car_id"
        const val PRICE = "price"
        const val BRANCH = "branch"
        const val SHOW_EMPLOYEE = "show_employee"
        const val EMPLOYEE = "employee"
        const val LIST_CARS = "list_cars"
        const val LIST_SERVICES = "list_services"
        const val AUTH = "is_auth"
        const val TOKEN = "token"
        const val IS_DELETE_TOKEN = "is delete"
        const val SERVICE = "service"
        const val SHOW_AUTH = "show_auth_page"

        const val CATEGORY = "category"

        private const val ROLE_CUSTOMER = "Клиент"
        const val ROLE_EMPLOYEE = "Сотрудник"
        private const val ROLE_ADMIN = "Администратор"
    }
}
