package com.xpand.videomeeting.listener

import com.xpand.videomeeting.models.User

interface UserListener {
    fun initiateVideoMeeting(user: User?)

    fun initiateAudioMeeting(user: User?)

    fun onMultipleUsersAction(isMultipleUsersSelected: Boolean?)
}