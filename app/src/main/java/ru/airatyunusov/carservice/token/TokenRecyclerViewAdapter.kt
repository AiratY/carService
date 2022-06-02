package ru.airatyunusov.carservice.token

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.utils.DateTimeHelper
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.model.TokenFirebaseModel
import java.time.LocalDateTime

class TokenRecyclerViewAdapter(private val onClick: (TokenFirebaseModel) -> Unit) :
    RecyclerView.Adapter<TokenRecyclerViewAdapter.ViewHolder>() {

    private var dataset: List<TokenFirebaseModel> = emptyList()
    private val nowDate = LocalDateTime.now()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val startDateTimeTextView: TextView = view.findViewById(R.id.startDateTimeTextView)
        private val endDateTimeTextView: TextView = view.findViewById(R.id.endDateTimeTextView)
        private val statusTextView: TextView = view.findViewById(R.id.statusTextView)
        private var currentToken: TokenFirebaseModel? = null

        init {
            view.setOnClickListener {
                currentToken?.let {
                    onClick(it)
                }
            }
        }

        fun bind(tokenFirebaseModel: TokenFirebaseModel) {
            currentToken = tokenFirebaseModel
            val startDateTime: LocalDateTime =
                LocalDateTime.parse(tokenFirebaseModel.startRecordDateTime)
            startDateTimeTextView.text = DateTimeHelper.convertToStringMyPattern(startDateTime)

            val endDateTime: LocalDateTime =
                LocalDateTime.parse(tokenFirebaseModel.endRecordDateTime)
            endDateTimeTextView.text = DateTimeHelper.convertToStringMyPattern(endDateTime)

            if (endDateTime < nowDate) {
                statusTextView.text = COMPLETE
            } else {
                statusTextView.text = NO_COMPLETE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.token_item_recyler_view, parent, false)
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

    companion object {
        private const val COMPLETE = "Завершён"
        private const val NO_COMPLETE = "Ожидается"
    }
}
