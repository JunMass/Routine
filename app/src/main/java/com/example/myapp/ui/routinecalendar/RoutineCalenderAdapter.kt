package com.example.myapp.ui.routinecalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.model.RoutineRecordWithTitle
import com.example.myapp.ui.routinedetail.RoutineRecordAdapter

class RoutineCalenderAdapter(
    private var items: List<RoutineRecordWithTitle>,
    private val onItemClick: (record: RoutineRecordWithTitle) -> Unit
) : RecyclerView.Adapter<RoutineCalenderAdapter.RecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder{
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.routine_item_without_buttons, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.tv_routine_title)
    }

    fun submitList(newItems: List<RoutineRecordWithTitle>) {
        items = newItems
        notifyDataSetChanged()
    }
}