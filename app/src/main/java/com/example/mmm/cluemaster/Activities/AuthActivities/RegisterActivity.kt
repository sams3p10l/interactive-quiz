package com.example.mmm.cluemaster.Activities.AuthActivities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.UserModel
import com.example.mmm.cluemaster.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var dbUsersRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbUsersRef = MainActivity.database.reference.child(Constants.NODE_USERS)
    }

    fun onRegister(view: View) {
        var register = true

        // Check if password has min length of 6
        val password = etPasswordRegister.text.toString().trim()
        if (TextUtils.isEmpty(password) || password.length < 6) {
            etPasswordRegister.error = "You must have 6 characters"
            register = false
        }

        // Check if Password and Confirm Password are same
        if (password != etConfirmPassword.text.toString().trim()) {
            etConfirmPassword.error = "Password do not match"
            register = false
        }

        // Check if email is valid
        val email = etEmail.text.toString().trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            register = false
        }

        if (register)
            registerToFirebase(email, password)

    }

    private fun registerToFirebase(email: String, password: String) {
        MainActivity.mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        saveUserToFirebase()

                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    } else
                        Toast.makeText(applicationContext, "Failed register", Toast.LENGTH_SHORT).show()
                }
    }

    private fun saveUserToFirebase() {
        val userID = MainActivity.mAuth.currentUser!!.uid

        dbUsersRef!!.child(userID).setValue(UserModel(etFullName.text.toString().trim(), etEmail.text.toString().trim(), etNick.text.toString().trim(), userID))
    }
}