// RoutineCalendarFragment.kt
package com.example.myapp.ui.routinecalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.FragmentRoutineCalendarBinding
import com.example.myapp.model.Weekday
import com.example.myapp.ui.RoutineViewModel
import java.time.LocalDate
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.example.myapp.R
import com.example.myapp.model.RoutineEntity
import com.example.myapp.model.RoutineRecordWithTitle

class RoutineCalendarFragment : Fragment() {
    private var _binding: FragmentRoutineCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineViewModel by activityViewModels()
    private lateinit var adapter: RoutineCalenderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineCalendarBinding.inflate(inflater, container, false)

        // 미래 선택 불가
        binding.calendarView.maxDate = System.currentTimeMillis()

        // RecyclerView 어댑터 설정
        adapter = RoutineCalenderAdapter(
            emptyList(),
            onItemClick = { routine -> showRoutineDetails(routine) }
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 달력 날짜 선택 리스너
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.getRecordsWithTitleForDate(selectedDate).observe(viewLifecycleOwner) {items ->
                adapter.submitList(items)
            }
        }

        // 초기값: 오늘 날짜 루틴 표시
        val today = LocalDate.now()
        viewModel.getRecordsWithTitleForDate(today).observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        return binding.root
    }

    private fun showRoutineDetails(routine: RoutineRecordWithTitle) {
        val bundle = Bundle().apply {
            putInt("routine_id", routine.record.routineId)
        }
        findNavController().navigate(R.id.action_routineCalendarFragment_to_routineDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}