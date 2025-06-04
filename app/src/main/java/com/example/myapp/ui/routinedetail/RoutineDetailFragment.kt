package com.example.myapp.ui.routinedetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.ui.RoutineViewModel
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.FragmentRoutineDetailBinding

class RoutineDetailFragment : Fragment() {
    private var _binding: FragmentRoutineDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineViewModel by activityViewModels()
    private lateinit var recordAdapter: RoutineRecordAdapter

    companion object {
        private const val ARG_ROUTINE_ID = "routine_id"
        fun newInstance(routineId: Int): RoutineDetailFragment {
            val fragment = RoutineDetailFragment()
            val args = Bundle()
            args.putInt(ARG_ROUTINE_ID, routineId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineDetailBinding.inflate(inflater, container, false)

        // 어댑터 생성
        recordAdapter = RoutineRecordAdapter(
            emptyList(),
            onItemClick = { record ->


            }
        )
        binding.recyclerView.adapter  = recordAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val routineId = arguments?.getInt(ARG_ROUTINE_ID) ?: -1

        // 루틴 제목 설정
        viewModel.getRoutineById(routineId).observe(viewLifecycleOwner) { routine ->
            routine?.let {
                (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = it.title
            }
        }

        // 기록 목록 관찰 및 어댑터에 전달
        viewModel.getRecordsForRoutine(routineId).observe(viewLifecycleOwner) { records ->
            recordAdapter.submitList(records)

        }
    }

}