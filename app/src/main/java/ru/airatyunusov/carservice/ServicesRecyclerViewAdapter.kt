package ru.airatyunusov.carservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.model.ServiceModel

class ServicesRecyclerViewAdapter : RecyclerView.Adapter<ServicesRecyclerViewAdapter.ViewHolder>() {
    private var dataset: List<ServiceModel> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameServiceTV: TextView = view.findViewById(R.id.nameServiceTextView)
        val hoursServiceTV: TextView = view.findViewById(R.id.hoursServiceTV)

        fun bind(serviceModel: ServiceModel) {
            nameServiceTV.text = serviceModel.name
            hoursServiceTV.text = serviceModel.hours.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_service_rv, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setDateSet(dataSet: List<ServiceModel>) {
        dataset = dataSet
        notifyDataSetChanged()
    }
}
