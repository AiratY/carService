package ru.airatyunusov.carservice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
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
import ru.airatyunusov.carservice.callbacks.TestCallBack
import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.ServiceModel

class EnrollFragment : Fragment(), TestCallBack {
    private val database =
        Firebase.database("https://carservice-93ef9-default-rtdb.europe-west1.firebasedatabase.app/")
    private val callBack: TestCallBack = this
    private var branchSpinner: Spinner? = null
    private val servicesListRecyclerViewAdapter = ServicesListRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enroll, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val carSpinner: Spinner = view.findViewById(R.id.carSpinner)
        branchSpinner = view.findViewById(R.id.branchSpinner)
        val serviceListRecyclerView: RecyclerView = view.findViewById(R.id.servicesListRecyclerView)
        val sendBtn: Button = view.findViewById(R.id.sendButton)

        serviceListRecyclerView.adapter = servicesListRecyclerViewAdapter

        val carList: Array<out String> =
            resources.getStringArray(R.array.carList) // arrayListOf("BMW", "LADA", "MERCEDES", "KIA")
        /*val branchList: Array<String> =
            arrayOf("СберСервис", "LADAСервис", "MERCEDESСервис", "KIAСервис")*/
        // val branchList: List<BranchModel> = loadBranchList()
        loadBranchList()
        loadListService()

        val carSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, carList
        )

        carSpinner.adapter = carSpinnerAdapter

        sendBtn.setOnClickListener {
            val list: List<ServiceModel> = servicesListRecyclerViewAdapter.getCheckedServices()
            val chooseBranch: String = branchSpinner?.selectedItem.toString()
            val chooseCar = carSpinner.selectedItem.toString()
            transferDataOfEnroll(list, chooseBranch, chooseCar)
        }
    }

    private fun loadListService() {
        val childName = "services"

        val listServices: MutableList<ServiceModel> = mutableListOf()

        var allCount = 0L
        var count = 1L

        val branchQuery =
            database.reference.child(childName).orderByChild("adminId").equalTo("jdbVeE5Y4iYmXofnRu7P2st8Tyq1")

        branchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (child in snapshot.children) {
                    val serviceModel = child.getValue<ServiceModel>()
                    serviceModel?.let { listServices.add(it) }
                    count++
                    Handler(Looper.getMainLooper()).post {
                        callBack.setListServices(listServices)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }

    private fun loadBranchList(): List<BranchModel> {
        val childName = "branch"

        val listBranch: MutableList<BranchModel> = mutableListOf()

        var allCount = 0L
        var count = 1L

        val branchQuery =
            database.reference.child(childName)

        branchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (child in snapshot.children) {
                    val branch = child.getValue<BranchModel>()
                    branch?.let { listBranch.add(it) }
                    count++
                    Handler(Looper.getMainLooper()).post {
                        callBack.setListBranch(listBranch)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })

        /*val branchDataSnapshot = branchQuery.get()
        while (count < allCount || !branchDataSnapshot.isComplete) {
            Thread.sleep(500)
        }*/

        return listBranch
    }

    private fun transferDataOfEnroll(listService: List<ServiceModel>, branch: String, car: String) {
        setFragmentResult(
            MainActivity.SHOW_SELECT_DATE_TIME,
            bundleOf(
                MainActivity.BUNDLE_KEY to true,
                LIST_SERVICE to listService,
                BRANCH_SERVICES to branch,
                CAR to car
            )
        )
    }

    companion object {
        const val LIST_SERVICE = "list_services"
        const val BRANCH_SERVICES = "branch_services"
        const val CAR = "car"

        /*val LIST_SERVICES: List<ServiceModel> =
            listOf(
                ServiceModel(1, "Замена масла в ДВС", 1),
                ServiceModel(2, "Замена масляного фильтра", 4),
                ServiceModel(3, "Замена воздушного фильтра", 8),
                ServiceModel(4, "Замена топливного фильтра", 10),
                ServiceModel(5, "Сброс межсервисных интервалов", 2),
                ServiceModel(6, "Замена тормозной жидкости", 1),
                ServiceModel(7, "Замена патрубков системы охлаждения", 3),
                ServiceModel(8, "Замена жидкости ГУР", 7),
                ServiceModel(9, "Диагностика форсунок (диагностика инжектора)", 6),
            )*/
    }

    override fun setListBranch(list: List<BranchModel>) {
        val branchSpinnerAdapter: ArrayAdapter<BranchModel> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, list
        )

        branchSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        branchSpinner?.adapter = branchSpinnerAdapter
    }

    override fun setListServices(listServices: List<ServiceModel>) {
        servicesListRecyclerViewAdapter.setDataSet(listServices)
    }

    /*"Промывка инжектора",
    "Промывка форсунок",
    "Диагностика свечей зажигания",
    "Замена свечей зажигания",
    "Замена масла",
    "Чистка инжектора",*/
}
