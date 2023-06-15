package com.xpand.videomeeting.activities

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.xpand.videomeeting.R
import com.xpand.videomeeting.utils.Constants
import com.xpand.videomeeting.utils.PreferenceManager


class SignIn : AppCompatActivity() {
    private var inputEmail: EditText? = null
    private  var inputPassword:EditText? = null
    private var buttonSignIn: MaterialButton? = null
    private var signInProgress: ProgressBar? = null
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        preferenceManager = PreferenceManager(applicationContext)
        signInProgress = findViewById(R.id.progressBarSignIn)

        checkPlayServices(this)

        if (preferenceManager!!.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.textSignUp).setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    applicationContext,
                    SignUp::class.java
                )
            )
        }

        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        buttonSignIn = findViewById(R.id.buttonSignIn)

        buttonSignIn!!.setOnClickListener(View.OnClickListener { view: View? ->
            if (inputEmail!!.text.toString().trim { it <= ' ' }
                    .isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail!!.text.toString()).matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
            } else if (inputPassword!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
            } else {
                signIn()
            }
        })
    }

    private fun signIn() {
        buttonSignIn!!.visibility = View.INVISIBLE
        signInProgress!!.visibility = View.VISIBLE
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, inputEmail!!.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, inputPassword!!.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result
                        .documents
                        .size > 0
                ) {
                    val documentSnapshot: DocumentSnapshot = task.result.documents[0]
                    preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager!!.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager!!.putString(
                        Constants.KEY_FIRST_NAME,
                        documentSnapshot.getString(Constants.KEY_FIRST_NAME)
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_LAST_NAME,
                        documentSnapshot.getString(Constants.KEY_LAST_NAME)
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_EMAIL,
                        documentSnapshot.getString(Constants.KEY_EMAIL)
                    )
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    signInProgress!!.visibility = View.INVISIBLE
                    buttonSignIn!!.visibility = View.VISIBLE
                    Toast.makeText(this, "Unable to sign in", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    fun checkPlayServices(context: Context?): Boolean {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context!!)
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i(
                TAG, "This device does not support Google Play Services. " +
                        "Push notifications are not supported"
            )
            return false
        }
        else{
            println("OK")
        }
        return true
    }
}