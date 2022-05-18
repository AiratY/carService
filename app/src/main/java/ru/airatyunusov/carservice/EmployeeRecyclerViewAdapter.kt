package ru.airatyunusov.carservice

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.model.Employee

class EmployeeRecyclerViewAdapter(private val onClick: (Employee) -> Unit) : RecyclerView.Adapter<EmployeeRecyclerViewAdapter.ViewHolder>() {
    private var dataset: List<Employee> = emptyList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.iconImageView)
        private val name: TextView = view.findViewById(R.id.nameTextView)
        private val desc: TextView = view.findViewById(R.id.descTextView)
        private var currentEmployee: Employee? = null

        init {
            icon.setImageResource(R.drawable.ic_baseline_engineering_24)
            view.setOnClickListener {
                currentEmployee?.let {
                    onClick(it)
                }
            }
        }

        fun bind(employee: Employee) {
            currentEmployee = employee
            name.text = employee.toString()
            val textCategory = "Категория:\n${employee.category}"
            desc.text = textCategory
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

    @SuppressLint("NotifyDataSetChanged")
    fun setDateSet(dataSet: List<Employee>) {
        dataset = dataSet
        notifyDataSetChanged()
    }
}
