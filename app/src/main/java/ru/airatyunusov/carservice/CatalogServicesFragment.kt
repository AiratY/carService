package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.callbacks.ServicesCallBack
import ru.airatyunusov.carservice.model.ServiceModel
import java.lang.ref.WeakReference

class CatalogServicesFragment : BaseFragment(), ServicesCallBack {

    private var recyclerView: RecyclerView? = null
    private var serviceAdapter: ServicesRecyclerViewAdapter? = null

    private var titleService: TextView? = null
    private var addService: TextView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catalog_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_SERVICES)
        setMenuWithExit()

        recyclerView = view.findViewById(R.id.listServicesRecyclerView)
        serviceAdapter = ServicesRecyclerViewAdapter { serviceModel -> changeService(serviceModel) }
        addService = view.findViewById(R.id.addServicesTextView)
        titleService = view.findViewById(R.id.titleListServicesTextView)
        progressBar = view.findViewById(R.id.catalogServicesProgressBar)
        recyclerView?.adapter = serviceAdapter

        loadListServices(this)

        addService?.setOnClickListener {
            openFragmentToAddServices()
        }
    }

    /**
     * Осуществляет переход на другой фрагмент для изменения данных услуги
     * */

    private fun changeService(serviceModel: ServiceModel) {
        setFragmentResult(
            MainActivity.SHOW_ADD_SERVICE,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                MainActivity.SERVICE to serviceModel
            )
        )
    }

    /**
     * Осуществляет переход на фрагмент добавления услуги
     * */

    private fun openFragmentToAddServices() {
        setFragmentResult(
            MainActivity.SHOW_ADD_SERVICE,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
            )
        )
    }

    /**
     * Загружает список оказываемых услуг
     * */

    private fun loadListServices(callBack: ServicesCallBack) {
        val weakReferenceCallBack = WeakReference(callBack)
        val childName = getString(R.string.services_firebase_key)
        val adminId = getUserId()

        val listServices: MutableList<ServiceModel> = mutableListOf()

        val query = reference.child(childName).orderByChild("adminId").equalTo(adminId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val serviceModel = child.getValue<ServiceModel>()
                    serviceModel?.let { listServices.add(it) }

                }
                weakReferenceCallBack.get()?.setListServices(listServices)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    /**
     * Скрывает ProgressBar
     * */

    private fun goneProgressBar() {
        progressBar?.visibility = View.GONE
    }

    companion object {
        private const val TITLE_SERVICES = "Услуги"
    }

    /**
     * Обновляет список услуг
     * */

    override fun setListServices(list: List<ServiceModel>) {
        goneProgressBar()
        visibleAllViews()
        serviceAdapter?.setDateSet(list)
    }

    override fun isShowBottomNavigationView(): Boolean {
        return true
    }

    /**
     * показывает все виджеты
     * */

    private fun visibleAllViews() {
        titleService?.visibility = View.VISIBLE
        addService?.visibility = View.VISIBLE
    }
}
