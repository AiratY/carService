package ru.airatyunusov.carservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.model.ServiceModel

class ServicesListRecyclerViewAdapter :
    RecyclerView.Adapter<ServicesListRecyclerViewAdapter.ViewHolder>() {

    private var dataset: List<ServiceModel> = mutableListOf()
    private var checkedServicesList: MutableList<ServiceModel> = mutableListOf()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val servicesCheckBox: CheckBox = view.findViewById(R.id.servicesCheckBox)

        init {
            servicesCheckBox.setOnClickListener{
                val nameService = servicesCheckBox.text.toString()
                val services = dataset.first { it.name == nameService }
                if (servicesCheckBox.isChecked){
                    checkedServicesList.add(services)
                } else {
                    checkedServicesList.remove(services)
                }
            }
        }

        fun bind(services: ServiceModel) {
            servicesCheckBox.text = services.name
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
    }

    fun getCheckedServices(): List<ServiceModel>{
        return checkedServicesList
    }
}