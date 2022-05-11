package ru.airatyunusov.carservice

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import ru.airatyunusov.carservice.model.BranchModel
import ru.airatyunusov.carservice.model.Employee
import ru.airatyunusov.carservice.model.FirebaseHelper
import java.time.LocalTime

class DetailBranchFragment : Fragment() {

    private val reference = FirebaseHelper().getDatabaseReference()
    private var deleteBtn: Button? = null
    private var branchModel: BranchModel? = null
    private var childName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_branch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childName = getString(R.string.branch_firebase_key)

        val nameBranchEditText: EditText = view.findViewById(R.id.nameBranchEditText)
        val addressEditText: EditText = view.findViewById(R.id.addressEditText)
        val phoneBranchEditText: EditText = view.findViewById(R.id.phoneBranchEditText)
        val saveBtn: Button = view.findViewById(R.id.saveBranchButton)
        val startTimeTimePicker: TimePicker = view.findViewById(R.id.startTimeTimePicker)
        val endTimeTimePicker: TimePicker = view.findViewById(R.id.endTimeTimePicker)
        deleteBtn = view.findViewById(R.id.deleteBranchBtn)

        startTimeTimePicker.setIs24HourView(true)
        startTimeTimePicker.minute = 0
        endTimeTimePicker.setIs24HourView(true)
        endTimeTimePicker.minute = 0

        goneDeleteBtn()

        arguments?.let {
            branchModel = it.get(BRANCH) as? BranchModel
            branchModel?.apply {
                nameBranchEditText.setText(name)
                addressEditText.setText(address)
                phoneBranchEditText.setText(phone)
                visibleDeleteBtn()

                val startTime: LocalTime = LocalTime.parse(startTime)
                val endTime: LocalTime = LocalTime.parse(endTime)
                startTimeTimePicker.hour = startTime.hour
                startTimeTimePicker.minute = startTime.minute
                endTimeTimePicker.hour = endTime.hour
                endTimeTimePicker.minute = endTime.minute
            }
        }

        saveBtn.setOnClickListener {
            val name = nameBranchEditText.text.toString()
            val address = addressEditText.text.toString()
            val phone: Long = phoneBranchEditText.text.toString().toLong()
            val startTime: LocalTime = LocalTime.of(startTimeTimePicker.hour, startTimeTimePicker.minute)
            val endTime: LocalTime = LocalTime.of(endTimeTimePicker.hour, endTimeTimePicker.minute)

            if (name.isEmpty() || address.isEmpty() || (phone == 0L)) {
                Toast.makeText(
                    requireContext(),
                    "Поля для ввода не должны бысь пустыми",
                    Toast.LENGTH_LONG
                ).show()
            } else if (phone.toString().length != 11) {
                Toast.makeText(
                    requireContext(),
                    "Номер телефона должен содержать 11 цифр",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (branchModel != null) {
                    branchModel?.apply {
                        updateBranch(BranchModel(id, adminId, name, address, phone.toString(), startTime.toString(), endTime.toString()))
                    }
                } else {
                    saveBranch(getAdminId(), name, address, phone.toString(), startTime.toString(), endTime.toString())
                }
                returnOnAdminFragment()
            }
        }

        deleteBtn?.setOnClickListener {
            removeBranch()
            returnOnAdminFragment()
        }
    }

    private fun updateBranch(branch: BranchModel) {
        val key = branch.id
        val childUpdates = hashMapOf<String, Any>(
            "/$childName/$key" to branch
        )
        reference.updateChildren(childUpdates)
    }

    private fun removeBranch() {
        val key = branchModel?.id
        key?.let {
            val query = reference.child("employees").orderByChild("branchId").equalTo(it)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val employee = child.getValue<Employee>()
                        employee?.let {
                            reference.child("employees").child(employee.id).removeValue()
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

    private fun goneDeleteBtn() {
        deleteBtn?.visibility = View.GONE
    }

    private fun visibleDeleteBtn() {
        deleteBtn?.visibility = View.VISIBLE
    }

    private fun returnOnAdminFragment() {
        setFragmentResult(
            MainActivity.SHOW_ADMIN_FRAGMENT,
            bundleOf(MainActivity.BUNDLE_KEY to true)
        )
    }

    private fun getAdminId(): String {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.user_data_sharedPreference),
            Context.MODE_PRIVATE
        )
        val userId = getString(R.string.user_id_key_SP)
        return sharedPreferences.getString(userId, "") ?: ""
    }

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
        private const val BRANCH = "branch"

        fun newInstance(branchModel: BranchModel): DetailBranchFragment {
            return DetailBranchFragment().apply {
                arguments = bundleOf(BRANCH to branchModel)
            }
        }
    }
}
