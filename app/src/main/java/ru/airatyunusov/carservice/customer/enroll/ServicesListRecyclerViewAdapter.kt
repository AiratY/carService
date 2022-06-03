package ru.airatyunusov.carservice.customer.enroll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.model.ServiceModel

class ServicesListRecyclerViewAdapter(private val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<ServicesListRecyclerViewAdapter.ViewHolder>() {

    private var dataset: List<ServiceModel> = mutableListOf()
    private var checkedServicesList: MutableList<ServiceModel> = mutableListOf()
    private var sum = 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val servicesCheckBox: CheckBox = view.findViewById(R.id.servicesCheckBox)
        private val priceTextView: TextView = view.findViewById(R.id.priceServiceTextView)

        init {
            servicesCheckBox.setOnClickListener {
                val nameService = servicesCheckBox.text.toString()
                val services = dataset.first { it.name == nameService }
                if (servicesCheckBox.isChecked) {
                    checkedServicesList.add(services)
                    sum += services.price
                } else {
                    checkedServicesList.remove(services)
                    sum -= services.price
                }
                onClick(sum)
            }
        }

        fun bind(services: ServiceModel) {
            servicesCheckBox.text = services.name
            priceTextView.text = services.price.toString() + "руб."
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.services_item_on_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setDataSet(dataset: List<ServiceModel>) {
        this.dataset = dataset
        notifyDataSetChanged()
    }

    fun getCheckedServices(): List<ServiceModel> {
        return checkedServicesList
    }
}
