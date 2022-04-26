package ru.airatyunusov.carservice.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.callbacks.AdminCallBack

class BranchRecyclerViewAdapter(val callback: AdminCallBack) : RecyclerView.Adapter<BranchRecyclerViewAdapter.ViewHolder>() {
    private var dataset: List<BranchModel> = emptyList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.iconImageView)
        private val name: TextView = view.findViewById(R.id.nameTextView)
        private val desc: TextView = view.findViewById(R.id.descTextView)
        private var currentBranch: BranchModel? = null

        init {
            icon.setImageResource(R.drawable.ic_baseline_business_24)
            view.setOnClickListener {
                currentBranch?.let {
                    callback.onClickBranchs(it)
                }
            }
        }

        fun bind(branchModel: BranchModel) {
            currentBranch = branchModel
            name.text = branchModel.name
            desc.text = "${branchModel.address}\nТел: ${branchModel.phone}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setDateSet(dataSet: List<BranchModel>) {
        dataset = dataSet
    }
}
