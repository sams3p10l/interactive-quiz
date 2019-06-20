package com.example.mmm.cluemaster.Activities.PlayingActivities

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.FinalResultFragment
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.GameFragment
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.ScoreboardFragment
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.ScoreboardFragment.Test.myScore
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.ScoreboardFragment.Test.player1score
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.ScoreboardFragment.Test.player2score
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.ScoreboardFragment.Test.player3score
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.ScoreboardFragment.Test.player4score
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.ScoreboardFragment.Test.player5score
import com.example.mmm.cluemaster.Adapters.FragmentAdapter
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.localDataScoreboard
import com.example.mmm.cluemaster.PresentationService
import com.example.mmm.cluemaster.R
import com.example.mmm.cluemaster.Utils
import com.google.android.gms.nearby.connection.Payload
import kotlinx.android.synthetic.main.activity_gameroom.*

class GameRoomActivity : AppCompatActivity(), GameCallback {

    private lateinit var mViewPager: ViewPager
    private lateinit var gamePIN: String
    private lateinit var fragBundle: Bundle
    lateinit var FRCallback: FinalResultCallback
    private val TAG: String = "GameRoomActivity"
    private val localAdapter = FragmentAdapter(supportFragmentManager)

    private val gameFragmentClass = GameFragment()
    private val scoreboardFragmentClass = ScoreboardFragment()
    private val finalResultFragmentClass = FinalResultFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameroom)

        fragBundle = Bundle()
        gamePIN = intent.getStringExtra(Constants.INTENT_MSG_PIN).toString()
        fragBundle.putString(Constants.BUNDLE_PIN2FRAG, gamePIN)

        mViewPager = gameroom_container

        setupViewPager(mViewPager, fragBundle)

        FRCallback = finalResultFragmentClass
    }

    override fun onStart() {
        super.onStart()

        Utils.scoreboardInit(gamePIN)

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(p0: Int) {
                when (p0) {
                    1 -> {
                        for (player in localDataScoreboard.scoreboard) {
                            if (player.name.equals(MainActivity.logUser.nickname))
                                myScore = "Your score is ${player.points}"
                        }

                        when (Utils.allPlayers.size) {
                            1 -> {
                                player1score = "${localDataScoreboard.scoreboard[0].name}  ${localDataScoreboard.scoreboard[0].points}"
                            }
                            2 -> {
                                player1score = "${localDataScoreboard.scoreboard[0].name}  ${localDataScoreboard.scoreboard[0].points}"
                                player2score = "${localDataScoreboard.scoreboard[1].name}  ${localDataScoreboard.scoreboard[1].points}"
                            }
                            3 -> {
                                player1score = "${localDataScoreboard.scoreboard[0].name}  ${localDataScoreboard.scoreboard[0].points}"
                                player2score = "${localDataScoreboard.scoreboard[1].name}  ${localDataScoreboard.scoreboard[1].points}"
                                player3score = "${localDataScoreboard.scoreboard[2].name}  ${localDataScoreboard.scoreboard[2].points}"
                            }
                            4 -> {
                                player1score = "${localDataScoreboard.scoreboard[0].name}  ${localDataScoreboard.scoreboard[0].points}"
                                player2score = "${localDataScoreboard.scoreboard[1].name}  ${localDataScoreboard.scoreboard[1].points}"
                                player3score = "${localDataScoreboard.scoreboard[2].name}  ${localDataScoreboard.scoreboard[2].points}"
                                player4score = "${localDataScoreboard.scoreboard[3].name}  ${localDataScoreboard.scoreboard[3].points}"
                            }
                            5 -> {
                                player1score = "${localDataScoreboard.scoreboard[0].name}  ${localDataScoreboard.scoreboard[0].points}"
                                player2score = "${localDataScoreboard.scoreboard[1].name}  ${localDataScoreboard.scoreboard[1].points}"
                                player3score = "${localDataScoreboard.scoreboard[2].name}  ${localDataScoreboard.scoreboard[2].points}"
                                player4score = "${localDataScoreboard.scoreboard[3].name}  ${localDataScoreboard.scoreboard[3].points}"
                                player5score = "${localDataScoreboard.scoreboard[4].name}  ${localDataScoreboard.scoreboard[4].points}"
                            }
                            else -> {
                                player1score = "${localDataScoreboard.scoreboard[0].name}  ${localDataScoreboard.scoreboard[0].points}"
                                player2score = "${localDataScoreboard.scoreboard[1].name}  ${localDataScoreboard.scoreboard[1].points}"
                                player3score = "${localDataScoreboard.scoreboard[2].name}  ${localDataScoreboard.scoreboard[2].points}"
                                player4score = "${localDataScoreboard.scoreboard[3].name}  ${localDataScoreboard.scoreboard[3].points}"
                                player5score = "${localDataScoreboard.scoreboard[4].name}  ${localDataScoreboard.scoreboard[4].points}"
                            }
                        }
                    }
                    2 -> FRCallback.callSaveScore()
                }
            }

        })
    }

    override fun onStop() {
        super.onStop()
        GameFragment.questionNum = 0
    }

    private fun setupViewPager(viewPager: ViewPager, bundle2send: Bundle) {

        gameFragmentClass.arguments = bundle2send
        scoreboardFragmentClass.arguments = bundle2send
        finalResultFragmentClass.arguments = bundle2send

        localAdapter.addFragment(gameFragmentClass, "GameFragment")
        localAdapter.addFragment(scoreboardFragmentClass, "ScoreboardFragment")
        localAdapter.addFragment(finalResultFragmentClass, "FinalResultFragment")
        viewPager.adapter = localAdapter
    }

    override fun switchFragment(position: Int) {
        mViewPager.currentItem = position

        if (position == 0) {
            val ft = supportFragmentManager!!.beginTransaction()
            ft.detach(localAdapter.getItem(0)).attach(localAdapter.getItem(0)).commit()
        } else if (position == 1) {
            val ft = supportFragmentManager!!.beginTransaction()
            ft.detach(localAdapter.getItem(1)).attach(localAdapter.getItem(1)).commit()
        }
    }

    fun onScoreboardClick(view: View) {
        for (endpointId in PreGameRoomActivity.userEndpoints) {
            val byteArray = "startScoreboard".toByteArray(AndroidNearby.UTF_8)
            AndroidNearby.connectionsClient!!.sendPayload(
                    endpointId,
                    Payload.fromBytes(byteArray)
            )
            Log.d(TAG, "onScoreboardClick: $endpointId")
        }
        ScoreboardFragment.callback.switchFragment(1)
        if (PreGameRoomActivity.serviceRunning) {
            val switchViewIntent = Intent(this@GameRoomActivity, PresentationService::class.java)
            switchViewIntent.putExtra(Constants.INTENT_MSG_ID, Constants.SWITCH_TO_SCOREBOARD)
            startService(Intent(switchViewIntent).setAction(Constants.INTENT_TV_SWITCHVIEW))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (PreGameRoomActivity.isHost) {
            val byteArray = "disconnect".toByteArray(AndroidNearby.UTF_8)
            for (endpoint in PreGameRoomActivity.userEndpoints) {
                AndroidNearby.connectionsClient!!.sendPayload(
                        endpoint,
                        Payload.fromBytes(byteArray)
                )
            }

            Utils.removeMeFromGameroom(gamePIN, PreGameRoomActivity.isHost)
        } else {
            Log.d(TAG, "onBackPressed: STARTED")
            // Disconnect from all Endpoints
            for (endpoint in PreGameRoomActivity.userEndpoints) {
                Log.d(PreGameRoomActivity.TAG, "onBackPressed: Disconnect endpoint: $endpoint")
                AndroidNearby.connectionsClient!!.disconnectFromEndpoint(endpoint)
            }

            Utils.removeMeFromGameroom(gamePIN, PreGameRoomActivity.isHost)
        }
    }
}

