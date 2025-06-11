package com.example.myapp.ui.home

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.R
import com.example.myapp.databinding.DialogEditRoutineBinding
import com.example.myapp.databinding.FragmentHomeBinding
import com.example.myapp.model.Weekday
import com.example.myapp.model.RoutineEntity
import com.example.myapp.ui.RoutineViewModel
import androidx.navigation.fragment.findNavController
import com.example.myapp.util.FileUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import androidx.core.net.toUri
import com.example.myapp.weather.LocationProvider
import com.example.myapp.weather.WeatherRepository
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineViewModel by activityViewModels()
    private val weatherViewModel: WeatherViewModel by activityViewModels()
    private lateinit var adapter: RoutineAdapter

    private var selectedPhotoUri: Uri? = null
    private var imagePreview: ImageView? = null

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사진 선택 후 처리 로직
            val uri = result.data?.data
            selectedPhotoUri = uri
            imagePreview?.apply {
                visibility = View.VISIBLE
                setImageURI(uri)
            }
        }
    }

    private lateinit var locationProvider: LocationProvider
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                weatherViewModel.loadWeather(locationProvider, weatherRepository)
            } else {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.textToday.text = today

        adapter = RoutineAdapter(
            emptyList(),
            onItemClick = { routine -> showRoutineDetails(routine) },
            onAddClick = { showAddRoutineDialog() },
            onStartClick = { routine -> showRecordBottomSheet(routine) }
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.routines.observe(viewLifecycleOwner) { routines ->
            adapter.submitList(routines)
        }

        locationProvider = LocationProvider(requireActivity())
        weatherRepository = WeatherRepository(requireContext())

        // 날씨 LiveData observe
        weatherViewModel.weatherInfo.observe(viewLifecycleOwner) { info ->
            binding.weatherTextView.text = String.format(Locale.getDefault(), "%.1f°C \n %s", info.main.temp, info.name)
            Glide.with(this)
                .load(info.weather[0].icon)
                .into(binding.weatherIconImageView)
        }
        weatherViewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // 최초 1회만 loadWeather 호출
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            weatherViewModel.loadWeather(locationProvider, weatherRepository)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private fun showAddRoutineDialog() {
        val dialogBinding = DialogEditRoutineBinding.inflate(layoutInflater)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("루틴 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가", null)
            .setNegativeButton("취소", null)
            .show()

        // 기본 값 설정
        dialogBinding.editTitle.setText("")
        dialogBinding.switchActive.isChecked = true
        Weekday.entries.forEach { day ->
            dialogBinding.root.findViewWithTag<CheckBox>("cb_$day")?.isChecked = true
        }

        dialogBinding.timeButton.setOnClickListener {
            val currentTime = LocalTime.now()
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    val selectedTime = LocalTime.of(h, m)
                    dialogBinding.timeButton.text = selectedTime.toString()
                },
                currentTime.hour,
                currentTime.minute,
                true
            ).show()
        }

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {
                val title = dialogBinding.editTitle.text.toString().trim()
                if (title.isEmpty()) {
                    dialogBinding.editTitle.error = "제목을 입력하세요"
                    return@setOnClickListener
                }
                val repeatOn = Weekday.entries.filter { day ->
                    dialogBinding.root.findViewWithTag<CheckBox>("cb_$day")?.isChecked == true
                }.toSet().takeIf { it.isNotEmpty() } ?: setOf(LocalDate.now().dayOfWeek.let {
                    Weekday.valueOf(it.name)
                })
                val timeParts = dialogBinding.timeButton.text.split(":")
                val startTime = LocalTime.of(
                    timeParts[0].toInt(),
                    timeParts[1].toInt()
                )
                viewModel.addRoutine(title, repeatOn, startTime, true)
                dialog.dismiss()
            }

    }

    private fun showRoutineDetails(routine: RoutineEntity) {
        val bundle = Bundle().apply {
            putInt("routine_id", routine.id)
        }
        findNavController().navigate(R.id.action_homeFragment_to_routineDetailFragment, bundle)
    }

    private fun showRecordBottomSheet(routine: RoutineEntity) {
        val today = LocalDate.now()

        lifecycleScope.launch {
            // 오늘 날짜의 기록이 있는지 확인
            val existingRecord = viewModel.getTodayRecord(routine.id, today)

            // 다이얼로그 UI 구성
            val dialogView = layoutInflater.inflate(R.layout.dialog_record_routine, requireActivity().window.decorView as ViewGroup, false)
            val titleText = dialogView.findViewById<TextView>(R.id.tvRoutineTitle)
            val editDetail = dialogView.findViewById<EditText>(R.id.editDetail)
            val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

            titleText.text = routine.title

            // 이미지 미리보기 설정
            imagePreview = dialogView.findViewById(R.id.imagePreview)
            val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPhoto)

            btnSelectPhoto.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                photoPickerLauncher.launch(intent)
            }

            // 기존 기록이 있다면 상세 내용을 미리 채워넣기
            if (existingRecord != null) {
                editDetail.setText(existingRecord.detail ?: "")

                // 기존 이미지가 있다면 미리보기 설정
                if (existingRecord.photoUri != null) {
                    selectedPhotoUri = existingRecord.photoUri.toUri()
                    imagePreview?.apply {
                        visibility = View.VISIBLE
                        setImageURI(selectedPhotoUri)
                    }
                }
            }
            val dialog = BottomSheetDialog(requireContext())
            dialog.setContentView(dialogView)

            btnSave.setOnClickListener {
                val detail = editDetail.text.toString()
                if (detail.isEmpty()) {
                    editDetail.error = "상세 내용을 입력하세요"
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    val localUri = withContext(Dispatchers.IO) { // coroutine로 IO 작업 수행
                        selectedPhotoUri?.let {
                            FileUtils.copyUriToInternalStorage(requireContext(), it)
                        }
                    }
                    if (existingRecord != null) {
                        // 기존 기록이 있다면 업데이트
                        val updated = existingRecord.copy(
                            detail = detail,
                            photoUri = localUri?.toString() ?: existingRecord.photoUri
                        )
                        viewModel.updateRecord(updated)
                    } else {
                        // 새로운 기록 추가
                        viewModel.addRecord(
                            routineId = routine.id,
                            date = today,
                            detail = detail,
                            photoUri = localUri?.toString()
                        )
                    }
                    dialog.dismiss()
                }
            }
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
