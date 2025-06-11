package com.example.myapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.model.RoutineEntity

class RoutineAdapter(
    private var routines: List<RoutineEntity>,
    private val onItemClick: (routine: RoutineEntity) -> Unit,
    private val onAddClick: () -> Unit,
    private val onStartClick: (routine: RoutineEntity) -> Unit,
    private val onEditClick: (routine: RoutineEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_ADD_BUTTON = 1
    }

    override fun getItemCount(): Int = routines.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == routines.size) TYPE_ADD_BUTTON else TYPE_ITEM
    }

    // 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.routine_item, parent, false)
            RoutineViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_button, parent, false)
            AddButtonViewHolder(view)
        }
    }
    // 뷰 홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RoutineViewHolder && position < routines.size) {
            val routine = routines[position]
            holder.titleText.text = routine.title
            holder.itemView.setOnClickListener {
                onItemClick(routine)
            }
            // 스타트 버튼 클릭 리스너 설정
            val startButton = holder.itemView.findViewById<Button>(R.id.btn_start_routine)
            startButton.setOnClickListener {
                onStartClick(routine)
            }
            // 수정 버튼 클릭 리스너 설정
            val editButton = holder.itemView.findViewById<ImageButton>(R.id.btn_edit_routine)
            editButton.setOnClickListener {
                onEditClick(routine)
            }

        } else if (holder is AddButtonViewHolder) {
            holder.addButton.setOnClickListener {
                onAddClick()
            }
        }
    }

    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.tv_routine_title)
    }

    class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addButton: Button = itemView.findViewById(R.id.btnAddRoutine)
    }

    fun submitList(newRoutines: List<RoutineEntity>) {
        routines = newRoutines
        notifyDataSetChanged()
    }
}