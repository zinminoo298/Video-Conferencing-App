package com.xpand.videomeeting.utils

class Constants {

    companion object{
        val KEY_COLLECTION_USERS = "users"
        val KEY_FIRST_NAME = "first_name"
        val KEY_LAST_NAME = "last_name"
        val KEY_EMAIL = "email"
        val KEY_PASSWORD = "password"
        val KEY_USER_ID = "user_id"
        val KEY_FCM_TOKEN = "fcm_token"

        val KEY_PREFERENCE_NAME = "videoMeetingPreference"
        val KEY_IS_SIGNED_IN = "isSignedIn"

        val REMOTE_MSG_AUTHORIZATION = "Authorization"
        val REMOTE_MSG_CONTENT_TYPE = "Content-Type"

        val REMOTE_MSG_TYPE = "type"
        val REMOTE_MSG_INVITATION = "invitation"
        val REMOTE_MSG_MEETING_TYPE = "meetingType"
        val REMOTE_MSG_INVITER_TOKEN = "inviterToken"
        val REMOTE_MSG_DATA = "data"
        val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"

        val REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse"
        val REMOTE_MSG_INVITATION_ACCEPTED = "accepted"
        val REMOTE_MSG_INVITATION_REJECTED = "rejected"
        val REMOTE_MSG_INVITATION_CANCELLED = "cancelled"

        val REMOTE_MSG_MEETING_ROOM = "meetingRoom"
        val API_KEY_SERVER = "AAAAKFgkKyM:APA91bHTcUB91zK4HZEt8nz6SukuETnx63H2_N2nkgkrpX-E2ImEjFOLvOpnlOSm2Blxl6io1MiqloVpbZSA8wuZEK-MWYpTvIyIKcv2uelccDHpECc5YB3TgyvD2EmkiMaKpWpGTpif"
    }

    fun getRemoteMessageHeaders(): HashMap<String, String>? {
        val headers = HashMap<String, String>()
        headers[REMOTE_MSG_AUTHORIZATION] =
            "key=$API_KEY_SERVER"
        headers[REMOTE_MSG_CONTENT_TYPE] =
            "application/json"
        return headers
    }
}