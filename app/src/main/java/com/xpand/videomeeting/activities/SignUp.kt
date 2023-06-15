package com.xpand.videomeeting.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.xpand.videomeeting.R
import com.xpand.videomeeting.utils.Constants
import com.xpand.videomeeting.utils.PreferenceManager

class SignUp : AppCompatActivity() {
    private var inputFirstName: EditText? = null
    private  var inputLastName:EditText? = null
    private  var inputEmail:EditText? = null
    private  var inputPassword:EditText? = null
    private  var inputConfirmPassword:EditText? = null
    private var buttonSignUp: MaterialButton? = null
    private var signUpProgress: ProgressBar? = null
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        preferenceManager = PreferenceManager(applicationContext)
        signUpProgress = findViewById(R.id.progressBarSignUp)

        findViewById<View>(R.id.imgBack).setOnClickListener { view: View? -> onBackPressed() }
        findViewById<View>(R.id.textSignIn).setOnClickListener { view: View? -> onBackPressed() }

        inputFirstName = findViewById(R.id.inputFirstName)
        inputLastName = findViewById(R.id.inputLastName)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword)
        buttonSignUp = findViewById(R.id.buttonSignUp)

        buttonSignUp!!.setOnClickListener(View.OnClickListener { view: View? ->
            if (inputFirstName!!.text.toString().trim { it <= ' ' }
                    .isEmpty()) {
                Toast.makeText(this, "Enter first name", Toast.LENGTH_SHORT).show()
            } else if (inputLastName!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this, "Enter last name", Toast.LENGTH_SHORT).show()
            } else if (inputEmail!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail!!.text.toString()).matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
            } else if (inputPassword!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
            } else if (inputConfirmPassword!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this, "Confirm your password", Toast.LENGTH_SHORT)
                    .show()
            } else if (inputPassword!!.text.toString() != inputConfirmPassword!!.text
                    .toString()
            ) {
                Toast.makeText(
                    this,
                    "Password & confirm password must be same",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signUp()
            }
        })
    }

    private fun signUp() {
        buttonSignUp!!.visibility = View.INVISIBLE
        signUpProgress!!.visibility = View.VISIBLE
        val database = FirebaseFirestore.getInstance()
        val users = HashMap<String, Any>()
        users[Constants.KEY_FIRST_NAME] = inputFirstName!!.text.toString()
        users[Constants.KEY_LAST_NAME] = inputLastName!!.text.toString()
        users[Constants.KEY_EMAIL] = inputEmail!!.text.toString()
        users[Constants.KEY_PASSWORD] = inputPassword!!.text.toString()
        try{
            database.collection(Constants.KEY_COLLECTION_USERS)
                .add(users)
                .addOnSuccessListener { documentReference: DocumentReference ->
                    preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager!!.putString(Constants.KEY_USER_ID, documentReference.id)
                    preferenceManager!!.putString(
                        Constants.KEY_FIRST_NAME,
                        inputFirstName!!.text.toString()
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_LAST_NAME,
                        inputLastName!!.text.toString()
                    )
                    preferenceManager!!.putString(Constants.KEY_EMAIL, inputEmail!!.text.toString())
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener { e: Exception ->
                    buttonSignUp!!.visibility = View.VISIBLE
                    signUpProgress!!.visibility = View.INVISIBLE
                    Toast.makeText(this, "Error woi : " + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}