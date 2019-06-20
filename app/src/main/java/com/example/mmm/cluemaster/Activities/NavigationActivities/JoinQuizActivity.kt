package com.example.mmm.cluemaster.Activities.NavigationActivities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mmm.cluemaster.Activities.HomeActivity
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Activities.PlayingActivities.PreGameRoomActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class JoinQuizActivity : AppCompatActivity() {

    private var databasePINref = MainActivity.database.reference.child(Constants.NODE_ACTIVEGAMES)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joinquiz)

        createDialog(this)
    }

    private fun createDialog(context: Context) {
        val mBuilder = AlertDialog.Builder(context)
        val mView = layoutInflater.inflate(R.layout.dialog_enterpin, null)
        val field = mView.findViewById<EditText>(R.id.et_enterpin)
        val confirmButton = mView.findViewById<Button>(R.id.pin_confirm_button)
        val cancelButton = mView.findViewById<Button>(R.id.pin_cancel_button)

        confirmButton.setOnClickListener {
            if (field.text.toString().isEmpty())
                Toast.makeText(context, getString(R.string.pin_toast), Toast.LENGTH_LONG).show()
            else {
                val intent = Intent(context, PreGameRoomActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                databasePINref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.hasChild(field.text.toString().trim())) {
                            intent.putExtra(Constants.INTENT_MSG_PIN, field.text.toString().trim())
                            startActivity(intent)
                        } else
                            Toast.makeText(context, getString(R.string.no_active_game), Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(p0: DatabaseError) {}
                })
            }
        }

        cancelButton.setOnClickListener {
            val intent = Intent(context, HomeActivity::class.java)
            startActivity(intent)
        }

        mBuilder.setView(mView)
        val mDialog = mBuilder.create()
        mDialog.show()
    }
}

