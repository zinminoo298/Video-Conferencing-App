package com.xpand.videomeeting.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.xpand.videomeeting.R
import com.xpand.videomeeting.adapter.UserAdapter
import com.xpand.videomeeting.listener.UserListener
import com.xpand.videomeeting.models.User
import com.xpand.videomeeting.utils.Constants


class MainActivity : AppCompatActivity(),UserListener {
    private var preferenceManager:com.xpand.videomeeting.utils.PreferenceManager? = null
    private var users: ArrayList<User>? = null
    private var usersAdapter: UserAdapter? = null
    private var textErrorMessage: TextView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var imageConference: ImageView? = null
    private val REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceManager = com.xpand.videomeeting.utils.PreferenceManager(this)

        imageConference = findViewById(R.id.imageConference)

        val textView = findViewById<TextView>(R.id.textTitle)
        textView.text = java.lang.String.format(
            "%s %s",
            preferenceManager!!.getString(Constants.KEY_FIRST_NAME),
            preferenceManager!!.getString(Constants.KEY_LAST_NAME)
        )

        findViewById<View>(R.id.textSignOut).setOnClickListener { view: View? -> signOut() }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { result: String? ->
            result?.let { sendFCMTokenToDatabase(it) }
        }

        val usersRecyclerview = findViewById<RecyclerView>(R.id.recyclerViewUsers)
        textErrorMessage = findViewById(R.id.textErrorMessage)

        users = ArrayList()
        usersAdapter = UserAdapter(users!!,this)
        usersRecyclerview.adapter = usersAdapter

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout!!.setOnRefreshListener(OnRefreshListener { this.getUsers() })

        getUsers()
        checkForBatteryOptimizations()
    }

    private fun getUsers() {
        swipeRefreshLayout!!.isRefreshing = true
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                swipeRefreshLayout!!.isRefreshing = false
                val myUsersId = preferenceManager!!.getString(Constants.KEY_USER_ID)
                if (task.isSuccessful && task.result != null) {
                    users!!.clear()
                    for (documentSnapshot in task.result!!) {
                        if (myUsersId == documentSnapshot.id) {
                            continue
                        }
                        val user = User()
                        user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME)
                        user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME)
                        user.email = documentSnapshot.getString(Constants.KEY_EMAIL)
                        user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                        users!!.add(user)
                    }
                    if (users!!.size > 0) {
                        usersAdapter!!.notifyDataSetChanged()
                    } else {
                        textErrorMessage!!.text = String.format("%s", "No users available")
                        textErrorMessage!!.visibility = View.VISIBLE
                    }
                } else {
                    textErrorMessage!!.text = String.format("%s", "No users available")
                    textErrorMessage!!.visibility = View.VISIBLE
                }
            }
    }

    private fun sendFCMTokenToDatabase(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager!!.getString(Constants.KEY_USER_ID)!!
        )
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this@MainActivity,
                    "Unable to send token: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun signOut() {
        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show()
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager!!.getString(Constants.KEY_USER_ID)!!
        )
        val updates = HashMap<String, Any>()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener { aVoid: Void? ->
                preferenceManager!!.clearPreferences()
                startActivity(Intent(applicationContext, SignIn::class.java))
                finish()
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    this@MainActivity,
                    "Unable to sign out",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkForBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Warning")
                builder.setMessage("Battery optimization is enabled. It can interrupt running background services.")
                builder.setPositiveButton("Disable") { dialogInterface: DialogInterface?, i: Int ->
                    val intent =
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS)
                }
                builder.setNegativeButton(
                    "Cancel"
                ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                builder.create().show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            checkForBatteryOptimizations()
        }
    }

    override fun initiateVideoMeeting(user: User?) {
        if (user!!.token == null || user.token!!.trim().isEmpty()) {
            Toast.makeText(
                this,
                user.firstName + " " + user.lastName + " is not available for meeting",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val intent = Intent(applicationContext, OutgoingInvitationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "audio")
            startActivity(intent)
        }
    }

    override fun initiateAudioMeeting(user: User?) {
        if (user!!.token == null || user.token!!.trim().isEmpty()) {
            Toast.makeText(
                this,
                user.firstName + " " + user.lastName + " is not available for meeting",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val intent = Intent(applicationContext, OutgoingInvitationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "audio")
            startActivity(intent)
        }
    }

    override fun onMultipleUsersAction(isMultipleUsersSelected: Boolean?) {
        if (isMultipleUsersSelected!!) {
            imageConference!!.visibility = View.VISIBLE
            imageConference!!.setOnClickListener { view: View? ->
                val intent = Intent(
                    applicationContext,
                    OutgoingInvitationActivity::class.java
                )
                intent.putExtra("selectedUsers", Gson().toJson(usersAdapter!!.getSelectedUsers()))
                intent.putExtra("type", "video")
                intent.putExtra("isMultiple", true)
                startActivity(intent)
            }
        } else {
            imageConference!!.visibility = View.GONE
        }
    }
}