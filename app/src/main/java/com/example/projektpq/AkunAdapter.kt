package com.example.projektpq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projektpq.models.User

class AkunAdapter(
    private var userList: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<AkunAdapter.AkunViewHolder>() {

    private var filteredList: List<User> = userList

    inner class AkunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tv_username)
        val tvRole: TextView = itemView.findViewById(R.id.tv_role)
        val tvEmail: TextView = itemView.findViewById(R.id.tv_email)
        val tvPhone: TextView = itemView.findViewById(R.id.tv_phone)
        val tvId: TextView = itemView.findViewById(R.id.tv_id)
        val ivUserIcon: ImageView = itemView.findViewById(R.id.iv_user_icon)

        fun bind(user: User) {
            tvUsername.text = user.username
            tvRole.text = user.role
            tvId.text = "#${user.id_user}"

            // Set role badge color
            if (user.role == "SUPER ADMIN") {
                tvRole.setBackgroundResource(R.drawable.badge_super_admin)
            } else {
                tvRole.setBackgroundResource(R.drawable.badge_admin)
            }

            // Show/hide email
            if (user.email.isNotEmpty()) {
                tvEmail.text = user.email
                tvEmail.visibility = View.VISIBLE
            } else {
                tvEmail.visibility = View.GONE
            }

            // Show/hide phone
            if (user.nomor_telepon.isNotEmpty()) {
                tvPhone.text = user.nomor_telepon
                tvPhone.visibility = View.VISIBLE
            } else {
                tvPhone.visibility = View.GONE
            }

            // Click listener
            itemView.setOnClickListener {
                onItemClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AkunViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_akun, parent, false)
        return AkunViewHolder(view)
    }

    override fun onBindViewHolder(holder: AkunViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    // Filter function untuk search
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            userList
        } else {
            userList.filter {
                it.username.contains(query, ignoreCase = true) ||
                        it.role.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    // Update data
    fun updateData(newList: List<User>) {
        userList = newList
        filteredList = newList
        notifyDataSetChanged()
    }
}