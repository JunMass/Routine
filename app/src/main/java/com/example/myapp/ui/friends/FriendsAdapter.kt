package com.example.myapp.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.model.Friend

class FriendsAdapter(
    private val onDeleteClick: (Friend) -> Unit // 삭제 콜백 받음
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private var friends = listOf<Friend>()

    fun submitList(newList: List<Friend>) {
        friends = newList
        notifyDataSetChanged()
    }

    inner class FriendViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textUid: TextView = view.findViewById(R.id.textUid)
        val deleteButton: ImageButton = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.textUid.text = friend.uid
        holder.deleteButton.setOnClickListener {
            onDeleteClick(friend)
        }
    }

    override fun getItemCount(): Int = friends.size
}

