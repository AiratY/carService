package ru.airatyunusov.carservice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.Employee
import java.time.LocalTime

class DetailBranchFragment : BaseFragment() {
    private var branchModel: BranchModel? = null
    private var childName = ""

    private var nameBranchEditText: EditText? = null
    private var addressEditText: EditText? = null
    private var phoneBranchEditText: EditText? = null
    private var startTimeTimePicker: TimePicker? = null
    private var endTimeTimePicker: TimePicker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_branch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childName = getString(R.string.branch_firebase_key)

        setTitle(TITLE_BRANCH)
        showButtonBack()
        setListenerArrowBack()

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionExit -> {
                    signOut()
                    true
                }
                R.id.actionDelete -> {
                    removeBranch()
                    returnOnAdminFragment()
                    true
                }
                R.id.actionOk -> {
                    prepareToSaveBranch()
                    true
                }
                else -> {
                    false
                }
            }
        }

        nameBranchEditText = view.findViewById(R.id.nameBranchEditText)
        addressEditText = view.findViewById(R.id.addressEditText)
        phoneBranchEditText = view.findViewById(R.id.phoneBranchEditText)
        startTimeTimePicker = view.findViewById(R.id.startTimeTimePicker)
        endTimeTimePicker = view.findViewById(R.id.endTimeTimePicker)

        startTimeTimePicker?.setIs24HourView(true)
        startTimeTimePicker?.minute = 0
        endTimeTimePicker?.setIs24HourView(true)
        endTimeTimePicker?.minute = 0

        arguments?.let {
            branchModel = it.get(BRANCH) as? BranchModel
            branchModel?.apply {
                nameBranchEditText?.setText(name)
                addressEditText?.setText(address)
                phoneBranchEditText?.setText(phone)

                val startTime: LocalTime = LocalTime.parse(startTime)
                val endTime: LocalTime = LocalTime.parse(endTime)
                startTimeTimePicker?.hour = startTime.hour
                startTimeTimePicker?.minute = startTime.minute
                endTimeTimePicker?.hour = endTime.hour
                endTimeTimePicker?.minute = endTime.minute
            }
        }

        if (branchModel == null) {
            setMenu(R.menu.menu_save)
        } else {
            setMenu(R.menu.menu_save_delete)
        }
    }
    /**
     * Аодготавливает данные о филиале к сохранению в БД
     * */

    private fun prepareToSaveBranch() {
        val name = nameBranchEditText?.text.toString()
        val address = addressEditText?.text.toString()
        val phone: Long = phoneBranchEditText?.text.toString().toLong()
        val startTime =
            startTimeTimePicker?.let { LocalTime.of(it.hour, it.minute) }
        val endTime = endTimeTimePicker?.let { LocalTime.of(it.hour, it.minute) }

        if (name.isEmpty() || address.isEmpty() || (phone == 0L)) {
            Toast.makeText(
                requireContext(),
                MESSAGE_NOT_NULL_EDIT_TEXT,
                Toast.LENGTH_LONG
            ).show()
        } else if (phone.toString().length != 11) {
            Toast.makeText(
                requireContext(),
                MESSAGE_INCORRECT_PHONE,
                Toast.LENGTH_LONG
            ).show()
        } else {
            if (branchModel != null) {
                branchModel?.apply {
                    updateBranch(
                        BranchModel(
                            id,
                            adminId,
                            name,
                            address,
                            phone.toString(),
                            startTime.toString(),
                            endTime.toString()
                        )
                    )
                }
            } else {
                saveBranch(
                    getUserId(),
                    name,
                    address,
                    phone.toString(),
                    startTime.toString(),
                    endTime.toString()
                )
            }
            returnBack()
            // returnOnAdminFragment()
        }
    }
    /**
     * Возращает на главную админимтратора
     * */
    private fun returnOnAdminFragment() {
        setFragmentResult(
            MainActivity.SHOW_ADMIN_FRAGMENT,
            bundleOf(MainActivity.BUNDLE_KEY to true)
        )
    }

    /**
     * Обновляет данные о филиале в БД
     * */

    private fun updateBranch(branch: BranchModel) {
        val key = branch.id
        val childUpdates = hashMapOf<String, Any>(
            "/$childName/$key" to branch
        )
        reference.updateChildren(childUpdates)
    }

    /**
     * Удвляет данные о филиале
     * */

    private fun removeBranch() {
        val key = branchModel?.id
        key?.let {
            val query = reference.child(EMPLOYEES_CHILD_NAME).orderByChild(BRANCH_ID_CHILD_NAME).equalTo(it)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val employee = child.getValue<Employee>()
                        employee?.let {
                            reference.child(EMPLOYEES_CHILD_NAME).child(employee.id).removeValue()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(getString(R.string.FIREBASE_LOG_TAG), error.message)
                }
            })
            reference.child(childName).child(it).removeValue()
        }
    }

    /**
     * Сохраняет данные в БД
     */

    private fun saveBranch(
        adminId: String,
        name: String,
        address: String,
        phone: String,
        startTime: String,
        endTime: String
    ) {
        val key = reference.child(childName).push().key ?: ""
        val branchModel = BranchModel(key, adminId, name, address, phone, startTime, endTime)
        updateBranch(branchModel)
    }

    companion object {
        private const val EMPLOYEES_CHILD_NAME = "employees"
        private const val BRANCH_ID_CHILD_NAME = "branchId"
        private const val MESSAGE_NOT_NULL_EDIT_TEXT = "Поля для ввода не должны быть пустыми"
        private const val MESSAGE_INCORRECT_PHONE = "Номер телефона должен содержать 11 цифр"
        private const val BRANCH = "branch"
        private const val TITLE_BRANCH = "Филиал"

        fun newInstance(branchModel: BranchModel): DetailBranchFragment {
            return DetailBranchFragment().apply {
                arguments = bundleOf(BRANCH to branchModel)
            }
        }
    }
}
