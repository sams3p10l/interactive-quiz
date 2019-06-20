package com.example.mmm.cluemaster.Activities.NavigationActivities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.activity_help.*

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        ivHelpExit.setOnClickListener {
            finish()
        }

        buAnswerA.setOnClickListener {
            Toast.makeText(this, "You selected answer A", Toast.LENGTH_SHORT).show()
        }

        buAnswerB.setOnClickListener {
            Toast.makeText(this, "You selected answer B", Toast.LENGTH_SHORT).show()
        }

        buAnswerC.setOnClickListener {
            Toast.makeText(this, "You selected answer C", Toast.LENGTH_SHORT).show()
        }

        buAnswerD.setOnClickListener {
            Toast.makeText(this, "You selected answer D", Toast.LENGTH_SHORT).show()
        }
    }
}
