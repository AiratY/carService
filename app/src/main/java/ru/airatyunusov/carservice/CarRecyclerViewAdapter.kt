package ru.airatyunusov.carservice

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.model.CarModel

class CarRecyclerViewAdapter(private val onClick: (CarModel) -> Unit) :
    RecyclerView.Adapter<CarRecyclerViewAdapter.ViewHolder>() {
    private var dataset: List<CarModel> = emptyList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val makeCarTextView = view.findViewById<TextView>(R.id.makeCarTextView)
        private val modelCarTextView = view.findViewById<TextView>(R.id.modelCarTextView)
        private val numberCatTextView = view.findViewById<TextView>(R.id.numberCatTextView)
        private val yearCarTextView = view.findViewById<TextView>(R.id.yearCarTextView)
        private var currentCarModel: CarModel = CarModel()

        init {
            view.setOnClickListener {
                onClick(currentCarModel)
            }
        }

        fun bind(carModel: CarModel) {
            currentCarModel = carModel
            makeCarTextView.text = carModel.make
            modelCarTextView.text = carModel.model
            numberCatTextView.text = carModel.numberCar
            yearCarTextView.text = carModel.year.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_car_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDateSet(dataSet: List<CarModel>) {
        dataset = dataSet
        notifyDataSetChanged()
    }
}
