package com.xpand.videomeeting.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.xpand.videomeeting.R
import com.xpand.videomeeting.listener.UserListener
import com.xpand.videomeeting.models.User
import java.lang.String

class UserAdapter(val users: ArrayList<User>,val userListener: UserListener): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    companion object{
        private var selectedUsers = ArrayList<User>()
        var usersListener:UserListener? = null
    }
    init {
        usersListener = userListener
    }
    fun getSelectedUsers(): List<User?>? {
        return selectedUsers
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_container_user,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.setUserData(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textFirstChar: TextView
        var textUsername: TextView
        var textEmail: TextView
        var imageAudioMeeting: ImageView
        var imageVideoMeeting: ImageView
        var imageSelected: ImageView
        var userContainer: ConstraintLayout

        init {
            textFirstChar = itemView.findViewById(R.id.textFirstChar)
            textUsername = itemView.findViewById(R.id.textUserName)
            textEmail = itemView.findViewById(R.id.textEmail)
            imageAudioMeeting = itemView.findViewById(R.id.imageAudioMeeting)
            imageVideoMeeting = itemView.findViewById(R.id.imageVideoMeeting)
            userContainer = itemView.findViewById(R.id.userContainer)
            imageSelected = itemView.findViewById(R.id.imageSelected)
        }

        fun setUserData(user: User) {
            textFirstChar.text = user.firstName!!.substring(0, 1)
            textUsername.text = String.format("%s %s", user.firstName, user.lastName)
            textEmail.text = user.email
            imageAudioMeeting.setOnClickListener { view: View? ->
                usersListener!!.initiateAudioMeeting(
                    user
                )
            }
            imageVideoMeeting.setOnClickListener { view: View? ->
                usersListener!!.initiateVideoMeeting(
                    user
                )
            }
            userContainer.setOnLongClickListener { view: View? ->
                if (imageSelected.visibility != View.VISIBLE) {
                    selectedUsers!!.add(user)
                    imageSelected.visibility = View.VISIBLE
                    imageVideoMeeting.visibility = View.GONE
                    imageAudioMeeting.visibility = View.GONE
                    usersListener!!.onMultipleUsersAction(true)
                }
                true
            }
            userContainer.setOnClickListener { view: View? ->
                if (imageSelected.visibility == View.VISIBLE) {
                    selectedUsers!!.remove(user)
                    imageSelected.visibility = View.GONE
                    imageVideoMeeting.visibility = View.VISIBLE
                    imageAudioMeeting.visibility = View.VISIBLE
                    if (selectedUsers!!.size == 0) {
                        usersListener!!.onMultipleUsersAction(false)
                    }
                } else {
                    if (selectedUsers!!.size > 0) {
                        selectedUsers!!.add(user)
                        imageSelected.visibility = View.VISIBLE
                        imageVideoMeeting.visibility = View.GONE
                        imageAudioMeeting.visibility = View.GONE
                    }
                }
            }
        }
    }
}