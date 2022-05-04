package ru.airatyunusov.carservice

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.model.TokenFirebaseModel

class TokenRecyclerViewAdapter(private val onClick: (TokenFirebaseModel) -> Unit): RecyclerView.Adapter<TokenRecyclerViewAdapter.ViewHolder>() {

    private var dataset: List<TokenFirebaseModel> = emptyList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTimeTextView: TextView = view.findViewById(R.id.dateTimeTextView)
        private var currentToken: TokenFirebaseModel? = null

        init {
            view.setOnClickListener {
                currentToken?.let{
                    onClick(it)
                }
            }
        }

        fun bind(tokenFirebaseModel: TokenFirebaseModel) {
            currentToken = tokenFirebaseModel
            dateTimeTextView.text = tokenFirebaseModel.toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_token, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDateSet(dataSet: List<TokenFirebaseModel>) {
        dataset = dataSet
        notifyDataSetChanged()
    }
}