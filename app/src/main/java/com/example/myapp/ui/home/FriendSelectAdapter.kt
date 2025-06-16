package com.example.myapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.model.Friend

class FriendSelectAdapter(
    private val friends: List<Friend>
) : RecyclerView.Adapter<FriendSelectAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkboxFriend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.checkBox.text = friend.uid
        holder.checkBox.isChecked = friend.isChecked
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            friend.isChecked = isChecked
        }
    }

    override fun getItemCount(): Int = friends.size
}
