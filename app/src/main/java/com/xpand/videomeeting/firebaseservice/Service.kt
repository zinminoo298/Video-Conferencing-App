package com.xpand.videomeeting.firebaseservice

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.xpand.videomeeting.activities.IncomingInvitationActivity
import com.xpand.videomeeting.utils.Constants


class Service: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val type: String = message.data[Constants.REMOTE_MSG_TYPE]!!

        if (type == Constants.REMOTE_MSG_INVITATION) {
            val intent = Intent(applicationContext, IncomingInvitationActivity::class.java)
            intent.putExtra(
                Constants.REMOTE_MSG_MEETING_TYPE,
                message.data[Constants.REMOTE_MSG_MEETING_TYPE]
            )
            intent.putExtra(
                Constants.KEY_FIRST_NAME,
                message.data[Constants.KEY_FIRST_NAME]
            )
            intent.putExtra(
                Constants.KEY_LAST_NAME,
                message.data[Constants.KEY_LAST_NAME]
            )
            intent.putExtra(
                Constants.KEY_EMAIL,
                message.data[Constants.KEY_EMAIL]
            )
            intent.putExtra(
                Constants.REMOTE_MSG_INVITER_TOKEN,
                message.data[Constants.REMOTE_MSG_INVITER_TOKEN]
            )
            intent.putExtra(
                Constants.REMOTE_MSG_MEETING_ROOM,
                message.data[Constants.REMOTE_MSG_MEETING_ROOM]
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else if (type == Constants.REMOTE_MSG_INVITATION_RESPONSE) {
            val intent = Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            intent.putExtra(
                Constants.REMOTE_MSG_INVITATION_RESPONSE,
                message.data[Constants.REMOTE_MSG_INVITATION_RESPONSE]
            )
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

}