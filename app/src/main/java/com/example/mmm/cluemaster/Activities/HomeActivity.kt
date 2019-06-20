package com.example.mmm.cluemaster.Activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.mmm.cluemaster.Activities.AuthActivities.LoginActivity
import com.example.mmm.cluemaster.Activities.NavigationActivities.*
import com.example.mmm.cluemaster.R
import com.example.mmm.cluemaster.Utils
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btJoinQuiz.setOnClickListener {
            val intent = Intent(this, JoinQuizActivity::class.java)
            startActivity(intent)
        }

        btHostQuiz.setOnClickListener {
            val intent = Intent(this, HostQuizActivity::class.java)
            startActivity(intent)
        }

        btMyquizzes.setOnClickListener {
            val intent = Intent(this, MyQuizzesActivity::class.java)
            startActivity(intent)
        }

        btHelp.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        btMyScore.setOnClickListener {
            val intent = Intent(this, MyScoreActivity::class.java)
            startActivity(intent)
        }

        Utils.getMeFromFB(MainActivity.loggedUser.uid)

    }

    // Set Up Menu in UI
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        when (id) {
            R.id.action_signout -> { // Sign out User
                MainActivity.mAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.action_share -> {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_SUBJECT, "CLUEMASTER")

                val message = "Come play Cluemaster with me!\n"

                i.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(i, "Choose one:"))
            }
        }
        return super.onOptionsItemSelected(item)
    }

}