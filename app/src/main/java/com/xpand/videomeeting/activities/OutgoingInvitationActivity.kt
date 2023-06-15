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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xpand.videomeeting.R
import com.xpand.videomeeting.api.ApiClient
import com.xpand.videomeeting.api.ApiService
import com.xpand.videomeeting.models.User
import com.xpand.videomeeting.utils.Constants
import com.xpand.videomeeting.utils.PreferenceManager
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions

import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.*

class OutgoingInvitationActivity : AppCompatActivity() {
    companion object{
        private var preferenceManager: PreferenceManager ? = null
        private var inviterToken: String? = null
        private var meetingRoom: String? = null
        private var meetingType: String? = null
        private var textFirstChar: TextView? = null
        private  var textUsername:TextView? = null
        private  var textEmail:TextView? = null
        private  var rejectionCount = 0
        private  var totalReceivers = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_invitation)

        preferenceManager = PreferenceManager(applicationContext)

        val imageMeetingType = findViewById<ImageView>(R.id.imageMeetingType)
        meetingType = intent.getStringExtra("type")

        if (meetingType != null) {
            if (meetingType == "video") {
                imageMeetingType.setImageResource(R.drawable.ic_video)
            } else {
                imageMeetingType.setImageResource(R.drawable.ic_audio)
            }
        }

        textFirstChar = findViewById(R.id.textFirstChar)
        textUsername = findViewById(R.id.textUserName)
        textEmail = findViewById(R.id.textEmail)

        val user: User? = intent.getSerializableExtra("user") as User?

        if (user != null) {
            textFirstChar!!.text = user.firstName!!.substring(0, 1)
            textUsername!!.text = java.lang.String.format("%s %s", user.firstName, user.lastName)
            textEmail!!.text = user.email
        }

        val imageStopInvitation = findViewById<ImageView>(R.id.imageStopInvitation)
        imageStopInvitation.setOnClickListener {
            if (intent.getBooleanExtra("isMultiple", false)) {
                val type = object :
                    TypeToken<ArrayList<User?>?>() {}.type
                val receivers: ArrayList<User> = Gson()
                    .fromJson<ArrayList<User>>(
                        intent.getStringExtra("selectedUsers"),
                        type
                    )
                cancelInvitation(null, receivers)
            } else {
                if (user != null) {
                    cancelInvitation(user.token, null)
                }
            }
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { result: String? ->
            if (result != null) {
                inviterToken = result
                if (meetingType != null) {
                    if (intent.getBooleanExtra("isMultiple", false)) {
                        val type = object :
                            TypeToken<ArrayList<User?>?>() {}.type
                        val receivers: ArrayList<User>? = Gson()
                            .fromJson<ArrayList<User>>(
                                intent.getStringExtra("selectedUsers"),
                                type
                            )
                        if (receivers != null) {
                            totalReceivers = receivers.size
                        }
                        initiateMeeting(meetingType!!, null, receivers)
                    } else {
                        if (user != null) {
                            totalReceivers = 1
                            initiateMeeting(meetingType!!, user.token, null)
                        }
                    }
                }
            }
        }
    }

    private fun initiateMeeting(
        meetingType: String,
        receiverToken: String?,
        receivers: java.util.ArrayList<User>?
    ) {
        try {
            val tokens = JSONArray()
            if (receiverToken != null) {
                tokens.put(receiverToken)
            }
            if (receivers != null && receivers.size > 0) {
                val userNames = StringBuilder()
                for (i in receivers.indices) {
                    tokens.put(receivers[i].token)
                    userNames.append(receivers[i].firstName).append(" ")
                        .append(receivers[i].lastName).append("\n")
                }
                textFirstChar!!.visibility = View.GONE
                textEmail!!.visibility = View.GONE
                textUsername!!.text = userNames.toString()
            }
            val body = JSONObject()
            val data = JSONObject()
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION)
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType)
            data.put(
                Constants.KEY_FIRST_NAME,
                preferenceManager!!.getString(Constants.KEY_FIRST_NAME)
            )
            data.put(
                Constants.KEY_LAST_NAME,
                preferenceManager!!.getString(Constants.KEY_LAST_NAME)
            )
            data.put(Constants.KEY_EMAIL, preferenceManager!!.getString(Constants.KEY_EMAIL))
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken)
            meetingRoom = preferenceManager!!.getString(Constants.KEY_USER_ID) + "_" + UUID.randomUUID().toString().substring(0, 5)
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom)
            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION)
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
                    if (type == Constants.REMOTE_MSG_INVITATION) {
                        Toast.makeText(
                            this@OutgoingInvitationActivity,
                            "Invitation sent successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (type == Constants.REMOTE_MSG_INVITATION_RESPONSE) {
                        Toast.makeText(
                            this@OutgoingInvitationActivity,
                            "Invitation cancelled",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@OutgoingInvitationActivity,
                        response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(this@OutgoingInvitationActivity, t.message, Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        })
    }

    private fun cancelInvitation(receiverToken: String?, receivers: java.util.ArrayList<User>?) {
        try {
            val tokens = JSONArray()
            if (receiverToken != null) {
                tokens.put(receiverToken)
            }
            if (receivers != null && receivers.size > 0) {
                for (user in receivers) {
                    tokens.put(user.token)
                }
            }
            val body = JSONObject()
            val data = JSONObject()
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(
                Constants.REMOTE_MSG_INVITATION_RESPONSE,
                Constants.REMOTE_MSG_INVITATION_CANCELLED
            )
            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private val invitationResponseReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null) {
                if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED) {
                    try {
                        val serverURL = URL("https://meet.jit.si")
                        val builder = JitsiMeetConferenceOptions.Builder()
                        builder.setServerURL(serverURL)
                        builder.setWelcomePageEnabled(false)
                        builder.setRoom(meetingRoom)
                        if (meetingType == "audio") {
                            builder.setVideoMuted(true)
                        }
                        JitsiMeetActivity.launch(this@OutgoingInvitationActivity, builder.build())
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@OutgoingInvitationActivity,
                            e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else if (type == Constants.REMOTE_MSG_INVITATION_REJECTED) {
                    rejectionCount += 1
                    if (rejectionCount == totalReceivers) {
                        Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show()
                        finish()
                    }
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