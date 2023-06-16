package com.xpand.videomeeting.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.xpand.videomeeting.R
import com.xpand.videomeeting.api.ApiClient
import com.xpand.videomeeting.api.ApiService
import com.xpand.videomeeting.utils.Constants
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class IncomingInvitationActivity : AppCompatActivity() {
    companion object{
        private var meetingType: String? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_invitation)

        val imageMeetingType = findViewById<ImageView>(R.id.imageMeetingType)
        meetingType = intent.getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE)

        if (meetingType != null) {
            if (meetingType == "video") {
                imageMeetingType.setImageResource(R.drawable.ic_video)
            } else {
                imageMeetingType.setImageResource(R.drawable.ic_audio)
            }
        }

        val textFirstChar = findViewById<TextView>(R.id.textFirstChar)
        val textUserName = findViewById<TextView>(R.id.textUserName)
        val textEmail = findViewById<TextView>(R.id.textEmail)

        val firstName = intent.getStringExtra(Constants.KEY_FIRST_NAME)
        if (firstName != null) {
            textFirstChar.text = firstName.substring(0, 1)
        }

        textUserName.text = String.format(
            "%s %s",
            firstName,
            intent.getStringExtra(Constants.KEY_LAST_NAME)
        )

        textEmail.text = intent.getStringExtra(Constants.KEY_EMAIL)

        val imageAcceptInvitation = findViewById<ImageView>(R.id.imageAcceptInvitation)
        imageAcceptInvitation.setOnClickListener { view: View? ->
            sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
            )
        }

        val imageRejectInvitation = findViewById<ImageView>(R.id.imageRejectInvitation)
        imageRejectInvitation.setOnClickListener { view: View? ->
            sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
            )
        }
    }

    private fun sendInvitationResponse(type: String, receiverToken: String?) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)
            val body = JSONObject()
            val data = JSONObject()
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type)
            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)
            sendRemoteMessage(body.toString(), type)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendRemoteMessage(remoteMessageBody: String, type: String) {
        ApiClient.client!!.create(ApiService::class.java).sendRemoteMessage(
            Constants().getRemoteMessageHeaders(), remoteMessageBody
        )!!.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED) {
                        try {
                            val serverURL = URL("https://meet.jit.si")
                            val builder = JitsiMeetConferenceOptions.Builder()
                            builder.setServerURL(serverURL)
                            builder.setWelcomePageEnabled(false)
                            builder.setRoom(intent.getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))
                            if (meetingType == "audio") {
                                builder.setVideoMuted(true)
                            }
                            JitsiMeetActivity.launch(
                                this@IncomingInvitationActivity,
                                builder.build()
                            )
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@IncomingInvitationActivity,
                                e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this@IncomingInvitationActivity,
                            "Invitation Rejected",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@IncomingInvitationActivity,
                        response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(this@IncomingInvitationActivity, t.message, Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        })
    }

    private val invitationResponseReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null) {
                if (type == Constants.REMOTE_MSG_INVITATION_CANCELLED) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver,
            IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseReceiver
        )
    }
}