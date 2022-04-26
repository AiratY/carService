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
import ru.airatyunusov.carservice.callbacks.AdminCallBack
import ru.airatyunusov.carservice.model.*
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class AdminFragment : Fragment(), AdminCallBack {
    private var listBranchRecyclerView: RecyclerView? = null

    private val database =
        Firebase.database("https://carservice-93ef9-default-rtdb.europe-west1.firebasedatabase.app/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val addBranchBtn: Button = view.findViewById(R.id.addBranchButton)
        val myListServiceBtn: Button = view.findViewById(R.id.myServiceListButton)
        listBranchRecyclerView = view.findViewById(R.id.listBranchRecyclerView)

        val executor = Executors.newSingleThreadExecutor()
        val callBack: WeakReference<AdminCallBack> = WeakReference(this)
        executor.execute {
            val listBranchs = loadListBranch()
            Handler(Looper.getMainLooper()).post {
                callBack.get()?.setListBranch(listBranchs)
            }
        }

        addBranchBtn.setOnClickListener {
            setFragmentResult(
                MainActivity.SHOW_DETAIL_BRANCH,
                bundleOf(MainActivity.BUNDLE_KEY to true)
            )
        }
        myListServiceBtn.setOnClickListener {
            setFragmentResult(
                MainActivity.SHOW_CATALOG_SERVICES,
                bundleOf(MainActivity.BUNDLE_KEY to true)
            )
        }
    }

    private fun loadListBranch(): List<BranchModel> {
        val childName = getString(R.string.branch_firebase_key)
        val adminId = getAdminId()

        val listBranch: MutableList<BranchModel> = mutableListOf()

        var allCount = 0L
        var count = 1L

        val branchQuery =
            database.reference.child(childName).orderByChild("adminId").equalTo(adminId)

        branchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCount = snapshot.childrenCount
                for (child in snapshot.children) {
                    val branch = child.getValue<BranchModel>()
                    branch?.let { listBranch.add(it) }
                    count++
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(FIREBASE_LOG_TAG, error.message)
            }
        })

        val branchDataSnapshot = branchQuery.get()
        while (count < allCount || !branchDataSnapshot.isComplete) {
            Thread.sleep(500)
        }

        return listBranch
    }

    private fun getAdminId(): String {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.admin_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        val userId = getString(R.string.user_id_key_SP)
        return sharedPreferences.getString(userId, "") ?: ""
    }

    override fun setListBranch(listBranchs: List<BranchModel>) {
        val branchAdapter = BranchRecyclerViewAdapter(this)
        branchAdapter.setDateSet(listBranchs)
        listBranchRecyclerView?.adapter = branchAdapter
    }

    override fun onClickBranchs(branch: BranchModel) {
        setFragmentResult(
            MainActivity.SHOW_BRANCH,
            bundleOf(MainActivity.BUNDLE_KEY to true, MainActivity.BRANCH_ITEM to branch)
        )
    }

    companion object {
        private const val FIREBASE_LOG_TAG = "Firebase"
        private const val EMPLOYEES_FIREBASE_KEY = "employees"
    }
}
