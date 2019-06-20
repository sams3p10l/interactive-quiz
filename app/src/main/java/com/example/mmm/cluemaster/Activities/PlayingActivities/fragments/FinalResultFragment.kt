package com.example.mmm.cluemaster.Activities.PlayingActivities.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mmm.cluemaster.Activities.HomeActivity
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Activities.PlayingActivities.AndroidNearby
import com.example.mmm.cluemaster.Activities.PlayingActivities.FinalResultCallback
import com.example.mmm.cluemaster.Activities.PlayingActivities.PreGameRoomActivity
import com.example.mmm.cluemaster.Activities.PlayingActivities.setTextForCast
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.MyScoreModel
import com.example.mmm.cluemaster.Models.localDataScoreboard
import com.example.mmm.cluemaster.R
import com.example.mmm.cluemaster.Utils
import com.google.android.gms.cast.CastRemoteDisplayLocalService
import com.google.android.gms.nearby.connection.Payload
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_finalresult.view.*
import java.text.SimpleDateFormat
import java.util.*



class FinalResultFragment : Fragment(), FinalResultCallback {

    private val TAG: String = "FinalResultFragment"
    lateinit var mView: View
    lateinit var pinFromGRA: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pinFromGRA = arguments!!.getString(Constants.BUNDLE_PIN2FRAG)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_finalresult, container, false)

        mView.btn_exitgame.setOnClickListener {
            if (PreGameRoomActivity.isHost) {
                val byteArray = "disconnect".toByteArray(AndroidNearby.UTF_8)
                for (endpoint in PreGameRoomActivity.userEndpoints) {
                    AndroidNearby.connectionsClient!!.sendPayload(
                            endpoint,
                            Payload.fromBytes(byteArray)
                    )
                }
                PreGameRoomActivity.userEndpoints.clear()
                if (PreGameRoomActivity.serviceRunning){
                    CastRemoteDisplayLocalService.stopService()
                    PreGameRoomActivity.serviceRunning = false
                }

                Utils.removeMeFromGameroom(PreGameRoomActivity.pin, PreGameRoomActivity.isHost)
                activity!!.finish()
            } else {
                Log.d(TAG, "btn_exitgame: STARTED")
                // Disconnect from all Endpoints
                for (endpoint in PreGameRoomActivity.userEndpoints) {
                    Log.d(PreGameRoomActivity.TAG, "onBackPressed: Disconnect endpoint: $endpoint")
                    AndroidNearby.connectionsClient!!.disconnectFromEndpoint(endpoint)
                }

                Utils.removeMeFromGameroom(PreGameRoomActivity.pin, PreGameRoomActivity.isHost)

            }

            GameFragment.questionNum = 0
            val intent = Intent(activity, HomeActivity::class.java)
            startActivity(intent)
        }

        return mView
    }

    override fun onStart() {
        super.onStart()

        setupTextViews()

    }

    override fun callSaveScore()
    {
        var localScore: Long = 0

        if(!PreGameRoomActivity.isHost)
        {
            for (player in localDataScoreboard.scoreboard)
            {
                if (player.name == MainActivity.logUser.nickname)
                    localScore = player.points
            }
            saveScore(localScore)
        }
    }

    private fun saveScore(score : Long)
    {
        val date = Calendar.getInstance().time
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val formattedDate = df.format(date)

        MainActivity.databaseRootRef.child(Constants.NODE_ACTIVEGAMES).child(pinFromGRA)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot) {
                        val qid = p0.child("quizID").value.toString()

                        MainActivity.databaseRootRef.child(Constants.NODE_QUIZZESLIST).addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(p0: DataSnapshot) {
                                for (quizholder in p0.children)
                                {
                                    if (quizholder.hasChild(qid))
                                    {
                                        val localVariable = quizholder.child(qid).child("title").value.toString()
                                        Utils.saveScoreToFirebase(MainActivity.logUser.UUID!!, MyScoreModel(formattedDate, localVariable, score))
                                    }
                                }
                            }

                            override fun onCancelled(p0: DatabaseError) {}
                        })
                    }
                    override fun onCancelled(p0: DatabaseError) {}
                })
    }

    private fun setupTextViews()
    {
        mView.tvFinalResultWinner.setTextForCast(localDataScoreboard.scoreboard[0].name,context!!)
        mView.tvLeaderboardThirdPlace.visibility = View.INVISIBLE
        mView.tvLeaderboardThirdPlace.visibility = View.INVISIBLE

        if (Utils.allPlayers.size == 2)
        {
            mView.tvLeaderboardThirdPlace.setTextForCast(localDataScoreboard.scoreboard[1].name, context!!)
            mView.tvLeaderboardThirdPlace.visibility = View.VISIBLE
        }
        else if (Utils.allPlayers.size > 2)
        {
            mView.tvLeaderboardThirdPlace.setTextForCast(localDataScoreboard.scoreboard[2].name, context!!)
            mView.tvLeaderboardThirdPlace.visibility = View.VISIBLE
        }

    }
}