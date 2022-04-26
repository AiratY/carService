package ru.airatyunusov.carservice

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.ServiceModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        when (intent.getStringExtra(MESSAGE_USER)) {
            SplashScreenActivity.CUSTOMER -> {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<EnrollFragment>(R.id.fragment_container_view)
                    addToBackStack(NAME_BACK_STACK)
                }
            }
            SplashScreenActivity.EMPLOYEE -> {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<AuthorizationFragment>(R.id.fragment_container_view)
                    addToBackStack(NAME_BACK_STACK)
                }
            }
            SplashScreenActivity.ADMIN -> {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<AdminFragment>(R.id.fragment_container_view)
                    addToBackStack(NAME_BACK_STACK)
                }
            }
        }

        /*val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        if (sharedPreferences.contains(AUTH) && sharedPreferences.getBoolean(AUTH, false)) {
            replaceFragment(EnrollFragment())
        } else {*/
        /*supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<AuthorizationFragment>(R.id.fragment_container_view)
            addToBackStack(NAME_BACK_STACK)
        }*/
        /* }*/

        supportFragmentManager.setFragmentResultListener(SHOW_ENROLL, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                replaceFragment(EnrollFragment())
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_SELECT_DATE_TIME, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                val listServicesModel: List<ServiceModel> =
                    bundle.get(EnrollFragment.LIST_SERVICE) as? List<ServiceModel> ?: emptyList()
                replaceFragment(SelectDateTimeFragment.newInstance(listServicesModel))
            }
        }

        supportFragmentManager.setFragmentResultListener(SHOW_ADMIN_FRAGMENT, this) { _, bundle ->
            if (bundle.getBoolean(BUNDLE_KEY)) {
                replaceFragment(AdminFragment())
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

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, fragment)
            addToBackStack(NAME_BACK_STACK)
        }
    }

    companion object {
        private const val NAME_BACK_STACK = "fragments"
        const val MESSAGE_USER = "action"
        const val SHOW_ENROLL = "show enroll"
        const val SHOW_ADMIN_FRAGMENT = "show admin"
        const val SHOW_SELECT_DATE_TIME = "show_select_date_time"
        const val BUNDLE_KEY = "show"
        const val SHOW_DETAIL_BRANCH = "show_detail_branch"
        const val SHOW_BRANCH = "show_branch"
        const val SHOW_CATALOG_SERVICES = "show_catalog"
        const val BRANCH_ITEM = "branch_item"
        const val BRANCH_ID = "branch_id"
        const val BRANCH = "branch"
        const val SHOW_EMPLOYEE = "show_employee"
        const val EMPLOYEE = "employee"

        const val AUTH = "is_auth"
    }
}
