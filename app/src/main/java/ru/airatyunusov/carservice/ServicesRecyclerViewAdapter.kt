package ru.airatyunusov.carservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.model.CarModel
import ru.airatyunusov.carservice.model.ServiceModel

class ServicesRecyclerViewAdapter(private val onClick: (ServiceModel) -> Unit) :
    RecyclerView.Adapter<ServicesRecyclerViewAdapter.ViewHolder>() {
    private var dataset: List<ServiceModel> = emptyList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameServiceTV: TextView = view.findViewById(R.id.nameServiceTextView)
        private val hoursServiceTV: TextView = view.findViewById(R.id.hoursServiceTV)
        private val priceServiceTV: TextView = view.findViewById(R.id.priceServiceTextView)
        private var currentService = ServiceModel()

        init {
            view.setOnClickListener {
                onClick(currentService)
            }
        }

        fun bind(serviceModel: ServiceModel) {
            currentService = serviceModel
            nameServiceTV.text = serviceModel.name
            hoursServiceTV.text = serviceModel.hours.toString() + "ч."
            priceServiceTV.text = serviceModel.price.toString() + "руб."
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
