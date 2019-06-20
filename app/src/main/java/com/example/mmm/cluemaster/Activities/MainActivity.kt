package com.example.mmm.cluemaster.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.mmm.cluemaster.Activities.AuthActivities.LoginActivity
import com.example.mmm.cluemaster.Models.UserModel
import com.example.mmm.cluemaster.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var mAuth: FirebaseAuth
        lateinit var database: FirebaseDatabase
        lateinit var databaseRootRef: DatabaseReference
        lateinit var loggedUser: FirebaseUser
        lateinit var mStorageRef: StorageReference
        lateinit var logUser: UserModel
    }

    var updateHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase init
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseRootRef = database.reference
        mStorageRef = FirebaseStorage.getInstance().reference

        updateHandler = Handler()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        var runnable: Runnable? = null

        if (currentUser != null) {
            runnable = Runnable {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                updateHandler!!.removeCallbacks(runnable)
            }
            loggedUser = currentUser
        } else {
            runnable = Runnable {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                updateHandler!!.removeCallbacks(runnable)
            }
        }
        updateHandler!!.postDelayed(runnable, 3000)
    }
}

