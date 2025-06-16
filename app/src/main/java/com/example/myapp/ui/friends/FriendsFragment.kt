package com.example.myapp.ui.friends

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.FragmentFriendsBinding
import com.example.myapp.model.Friend
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val friendsAdapter = FriendsAdapter { friend ->
        confirmAndDeleteFriend(friend.uid)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerViewFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFriends.adapter = friendsAdapter

        loadFriends()


        binding.buttonAddFriend.setOnClickListener {
            val friendUid = binding.editTextFriendUid.text.toString().trim()

            if (friendUid.isEmpty()) {
                Toast.makeText(requireContext(), "친구 ID를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
            val currentUid = prefs.getString("userId", null)

            if (currentUid == null) {
                Toast.makeText(requireContext(), "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 자기 자신을 친구로 추가하는 것 방지
            if (friendUid == currentUid) {
                Toast.makeText(requireContext(), "본인은 친구로 추가할 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()

            // 입력한 UID가 실제로 존재하는 사용자 ID인지 확인
            db.collection("users")
                .document(friendUid)
                .get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        Toast.makeText(requireContext(), "존재하지 않는 사용자 ID입니다", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // 존재하는 UID일 경우에만 친구 추가
                    val friend = hashMapOf("uid" to friendUid)
                    db.collection("users")
                        .document(currentUid)
                        .collection("friends")
                        .document(friendUid)
                        .set(friend)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "친구 추가 완료", Toast.LENGTH_SHORT).show()
                            binding.editTextFriendUid.setText("")
                            loadFriends()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "친구 추가 실패", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "사용자 확인 실패", Toast.LENGTH_SHORT).show()
                }
        }



    }

    private fun loadFriends() {
        val prefs = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        val currentUid = prefs.getString("userId", null)

        if (currentUid == null) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(currentUid)
            .collection("friends")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(Friend::class.java) }
                friendsAdapter.submitList(list)
            }
    }

    private fun confirmAndDeleteFriend(friendUid: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("친구 삭제")
            .setMessage("정말로 $friendUid 님을 친구 목록에서 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                performDeleteFriend(friendUid)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun performDeleteFriend(friendUid: String) {
        val prefs = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        val currentUid = prefs.getString("userId", null)

        if (currentUid == null) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUid)
            .collection("friends")
            .document(friendUid)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "친구 삭제 완료", Toast.LENGTH_SHORT).show()
                loadFriends()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "친구 삭제 실패", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}