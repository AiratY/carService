package ru.airatyunusov.carservice

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import ru.airatyunusov.carservice.callbacks.ServicesCallBack
import ru.airatyunusov.carservice.model.ServiceModel
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class CatalogServicesFragment : Fragment(), ServicesCallBack {

    private var recyclerView: RecyclerView? = null
    private var serviceAdapter: ServicesRecyclerViewAdapter? = null
    private val callBack: ServicesCallBack = this
    private val database =
        Firebase.database("https://carservice-93ef9-default-rtdb.europe-west1.firebasedatabase.app/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catalog_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.listServicesRecyclerView)
        serviceAdapter = ServicesRecyclerViewAdapter { serviceModel -> changeService(serviceModel) }
        val addService: Button = view.findViewById(R.id.addServicesButton)
        recyclerView?.adapter = serviceAdapter

        val executor = Executors.newSingleThreadExecutor()
        val callBack: WeakReference<ServicesCallBack> = WeakReference(this)
        executor.execute {
            val listService = loadListServices()
            Handler(Looper.getMainLooper()).post {
                callBack.get()?.setListServices(listService)
            }
        }

        addService.setOnClickListener {
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

    private fun loadListServices(): List<ServiceModel> {
        val childName = getString(R.string.services_firebase_key)
        val adminId = getAdminId()

        val listServices: MutableList<ServiceModel> = mutableListOf()

        var allCount = 0L
        var count = 1L

        val query =
            database.reference.child(childName).orderByChild("adminId").equalTo(adminId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (child in snapshot.children) {
                    val serviceModel = child.getValue<ServiceModel>()
                    serviceModel?.let { listServices.add(it) }
                    count++
                }
                Handler(Looper.getMainLooper()).post {
                    callBack.setListServices(listServices)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })

        val branchDataSnapshot = query.get()
        while (count < allCount || !branchDataSnapshot.isComplete) {
            Thread.sleep(500)
        }

        return listServices
    }

    private fun getAdminId(): String {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.user_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        val userId = getString(R.string.user_id_key_SP)
        return sharedPreferences.getString(userId, "") ?: ""
    }

    companion object {
    }

    override fun setListServices(list: List<ServiceModel>) {
        serviceAdapter?.setDateSet(list)
    }
}
