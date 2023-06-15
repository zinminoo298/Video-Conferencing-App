package com.xpand.videomeeting.firebaseservice

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class Service: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "TOKEN $token")
        println("TOKEN $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if(message.notification != null){
            Log.d("FCM","Remote message received ${message.notification!!.body}")
            println("Remote message received ${message.notification!!.body}")
        }
    }

}