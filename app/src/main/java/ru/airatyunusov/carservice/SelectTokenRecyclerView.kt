package ru.airatyunusov.carservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.model.TokenModel

class SelectTokenRecyclerView(private val onClick: (TokenModel) -> Unit) : RecyclerView.Adapter<SelectTokenRecyclerView.ViewHolder>() {
    private var dataset: List<TokenModel> = mutableListOf()
    private var prevView: View? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val startDateTimeTextView: TextView = view.findViewById(R.id.startDateTimeTextView)
        private val endDateTimeTextView: TextView = view.findViewById(R.id.endDateTimeTextView)
        private var currentToken: TokenModel? = null


        init {
            view.setOnClickListener {
                currentToken?.let(onClick)
                prevView?.setBackgroundResource(R.color.white)
                view.setBackgroundResource(R.color.grey)
                prevView = it
            }
        }

        fun bind(tokenModel: TokenModel) {
            currentToken = tokenModel
            startDateTimeTextView.text =
                DateTimeHelper.convertToStringMyPattern(tokenModel.startRecordDateTime)
            endDateTimeTextView.text =
                DateTimeHelper.convertToStringMyPattern(tokenModel.endRecordDateTime)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_generate_new_token, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setDataSet(dataset: List<TokenModel>) {
        this.dataset = dataset
        notifyDataSetChanged()
    }
}