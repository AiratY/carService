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
import ru.airatyunusov.carservice.callbacks.AdminCallBack
import ru.airatyunusov.carservice.model.*
import java.lang.ref.WeakReference

class AdminFragment : BaseFragment(), AdminCallBack {
    private var listBranchRecyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var titleNameTextView: TextView? = null
    private var nameCarServiceTextView: TextView? = null
    private var titleBranchTextView: TextView? = null
    private var addBranchTextView: TextView? = null
    private var branchAdapter: BranchRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOOLBAR)
        setMenuWithExit()

        addBranchTextView = view.findViewById(R.id.addNewBranchTextView)
        titleBranchTextView = view.findViewById(R.id.titleBranchTextView)
        titleNameTextView = view.findViewById(R.id.titleNameServiceTextView)
        nameCarServiceTextView = view.findViewById(R.id.nameCarServiceTextView)
        listBranchRecyclerView = view.findViewById(R.id.listBranchRecyclerView)
        progressBar = view.findViewById(R.id.adminProgressBar)
        branchAdapter = BranchRecyclerViewAdapter(this)
        listBranchRecyclerView?.adapter = branchAdapter

        nameCarServiceTextView?.text = getNameUser()

        loadListBranch(this)

        addBranchTextView?.setOnClickListener {
            setFragmentResult(
                MainActivity.SHOW_DETAIL_BRANCH,
                bundleOf(MainActivity.BUNDLE_KEY to true)
            )
        }
    }

    /**
     * Загружает список филиалов из БД
     * */

    private fun loadListBranch(callBack: AdminCallBack) {
        val weakReference = WeakReference(callBack)
        val childName = getString(R.string.branch_firebase_key)
        val adminId = getUserId()

        val branchQuery = reference.child(childName).orderByChild("adminId").equalTo(adminId)

        branchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listBranch: MutableList<BranchModel> = mutableListOf()
                for (child in snapshot.children) {
                    val branch = child.getValue<BranchModel>()
                    branch?.let { listBranch.add(it) }
                }
                weakReference.get()?.setListBranch(listBranch)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(FIREBASE_LOG_TAG, error.message)
            }
        })
    }

    /**
     * Скрывает ProgressBar
     * */

    private fun goneProgressBar() {
        progressBar?.visibility = View.GONE
    }

    /**
     * Обновляет данные
     * */

    override fun setListBranch(listBranchs: List<BranchModel>) {
        branchAdapter?.setDateSet(listBranchs)
        visibleAllViews()
        goneProgressBar()
    }

    /**
     * Показывает все виджеты
     * */

    private fun visibleAllViews() {
        titleNameTextView?.visibility = View.VISIBLE
        nameCarServiceTextView?.visibility = View.VISIBLE
        titleBranchTextView?.visibility = View.VISIBLE
        addBranchTextView?.visibility = View.VISIBLE
    }

    /**
     * Переходит на фрагмент с описанием филиала
     * */

    override fun transferOnDetailDescriptionBranchs(branch: BranchModel) {
        setFragmentResult(
            MainActivity.SHOW_BRANCH,
            bundleOf(MainActivity.BUNDLE_KEY to true, MainActivity.BRANCH_ITEM to branch)
        )
    }

    override fun isShowBottomNavigationView(): Boolean {
        return true
    }

    companion object {
        private const val FIREBASE_LOG_TAG = "Firebase"
        private const val TITLE_TOOLBAR = "Профиль администратора"
    }
}
