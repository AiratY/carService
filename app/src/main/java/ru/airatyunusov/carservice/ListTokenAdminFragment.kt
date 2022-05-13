package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.callbacks.ListBranchCallBack
import ru.airatyunusov.carservice.model.BranchModel
import java.lang.ref.WeakReference

class ListTokenAdminFragment : ListTokenFragment(), ListBranchCallBack {
    private var spinner: Spinner? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_token_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE_TOKEN)
        setMenuWithExit()

        spinner = view.findViewById(R.id.spinnerListToken)

        loadListBranch(this)

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val branch: BranchModel =
                    parent?.getItemAtPosition(position) as? BranchModel ?: BranchModel()
                branchId = branch.id
                loadListToken(branchId, this@ListTokenAdminFragment)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.e("SELECTED", "Ничего не выбранно")
            }
        }

    }

    /**
     * Загружает список филиалов из БД
     * */

    private fun loadListBranch(callBack: ListBranchCallBack) {
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
                Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
            }
        })
    }


    override fun setListBranch(list: List<BranchModel>) {
        val branchSpinnerAdapter: ArrayAdapter<BranchModel> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, list
        )
        branchSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner?.adapter = branchSpinnerAdapter
    }

    override fun isShowBottomNavigationView(): Boolean {
        return true
    }

    companion object {
        private const val TITLE_TOKEN = "Записи"
    }
}