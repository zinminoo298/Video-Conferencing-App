package com.xpand.videomeeting.activities

import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xpand.videomeeting.R
import com.xpand.videomeeting.models.User

class MainActivity : AppCompatActivity() {
    private val preferenceManager: PreferenceManager? = null
    private val users: List<User>? = null
//    private val usersAdapter: UsersAdapter? = null
    private val textErrorMessage: TextView? = null
    private val swipeRefreshLayout: SwipeRefreshLayout? = null
    private val imageConference: ImageView? = null
    private val REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}