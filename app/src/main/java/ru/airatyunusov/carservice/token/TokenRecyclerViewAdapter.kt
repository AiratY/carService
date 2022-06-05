package ru.airatyunusov.carservice.token

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.model.TokenFirebaseModel
import ru.airatyunusov.carservice.utils.DateTimeHelper
import java.lang.ref.WeakReference
import java.time.LocalDateTime

class TokenRecyclerViewAdapter(
    context: Context,
    private val onClick: (TokenFirebaseModel) -> Unit
) :
    RecyclerView.Adapter<TokenRecyclerViewAdapter.ViewHolder>() {

    private var dataset: List<TokenFirebaseModel> = emptyList()
    private val nowDate = LocalDateTime.now()
    private val weakReferenceContext = WeakReference(context)

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

            when {
                endDateTime < nowDate -> {
                    statusTextView.text = COMPLETE
                    // (itemView as? CardView)?.setCardBackgroundColor(R.color.light_grey)
                    weakReferenceContext.get()
                        ?.let {
                            (itemView as? CardView)?.setCardBackgroundColor(
                                ContextCompat.getColor(
                                    it,
                                    R.color.light_grey
                                )
                            )
                        }
                }
                startDateTime < nowDate -> {
                    statusTextView.text = PERFORMED
                    weakReferenceContext.get()
                        ?.let {
                            (itemView as? CardView)?.setCardBackgroundColor(
                                ContextCompat.getColor(
                                    it,
                                    R.color.light_yellow
                                )
                            )
                        }
                }
                else -> {
                    statusTextView.text = EXPECTED
                    weakReferenceContext.get()
                        ?.let {
                            (itemView as? CardView)?.setCardBackgroundColor(
                                ContextCompat.getColor(
                                    it,
                                    R.color.light_green
                                )
                            )
                        }
                }
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
        private const val EXPECTED = "Ожидается"
        private const val PERFORMED = "Выполняется"
    }
}
