package ru.airatyunusov.carservice.customer.enroll

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.utils.DateTimeHelper
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.model.TokenModel

class SelectTokenRecyclerView(private val onClick: (TokenModel) -> Unit) :
    RecyclerView.Adapter<SelectTokenRecyclerView.ViewHolder>() {
    private var dataset: List<TokenModel> = mutableListOf()
    private var selectTokenPosition: Int = RecyclerView.NO_POSITION

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val startDateTimeTextView: TextView = view.findViewById(R.id.startDateTimeTextView)
        private val endDateTimeTextView: TextView = view.findViewById(R.id.endDateTimeTextView)
        private var currentToken: TokenModel? = null


        init {
            view.setOnClickListener {
                currentToken?.let(onClick)
                notifyItemChanged(selectTokenPosition)
                selectTokenPosition = adapterPosition
                notifyItemChanged(selectTokenPosition)
            }

            /*if (adapterPosition == selectTokenPosition) {
                view.setBackgroundResource(R.color.purple_200)
            } else {
                view.setBackgroundResource(R.color.white)
            }*/

        }

        @SuppressLint("ResourceAsColor")
        fun bind(tokenModel: TokenModel) {
            currentToken = tokenModel
            startDateTimeTextView.text =
                DateTimeHelper.convertToStringMyPattern(tokenModel.startRecordDateTime)
            endDateTimeTextView.text =
                DateTimeHelper.convertToStringMyPattern(tokenModel.endRecordDateTime)

            val d = if (selectTokenPosition == adapterPosition) Color.GRAY else Color.WHITE
            (itemView as? CardView)?.setCardBackgroundColor(d)
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

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSet(dataset: List<TokenModel>) {
        this.dataset = dataset
        selectTokenPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }
}