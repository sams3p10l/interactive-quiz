package com.example.mmm.cluemaster.Activities.PlayingActivities.fragments

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.mmm.cluemaster.Activities.PlayingActivities.*
import com.example.mmm.cluemaster.Activities.PlayingActivities.AndroidNearby.context
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.localDataQuestions
import com.example.mmm.cluemaster.PresentationService
import com.example.mmm.cluemaster.R
import com.example.mmm.cluemaster.Utils
import com.google.android.gms.nearby.connection.Payload
import kotlinx.android.synthetic.main.fragment_scoreboard.*
import kotlinx.android.synthetic.main.fragment_scoreboard.view.*
import java.lang.Exception

class ScoreboardFragment : Fragment(), ScoreboardCallback {

    private val TAG: String = "ScoreboardFragment:"
    var pin: String = ""
    lateinit var mView: View

    companion object Test {
        var player1score = ""
        var player2score = ""
        var player3score = ""
        var player4score = ""
        var player5score = ""
        var myScore = ""

        lateinit var callback: GameCallback
        lateinit var scCallback: ScoreboardCallback

        fun callMe2(pin: String) {
            scCallback.scoreboardViewInit(pin)
        }

        fun startTimer() {
            scCallback.startTimerOverride()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pin = arguments!!.getString(Constants.BUNDLE_PIN2FRAG)!!
    }

    override fun startTimerOverride() {
        CountdownTimerTask().execute()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_scoreboard, container, false)

        mView.tv_firstplace.visibility = View.GONE
        mView.tv_secondplace.visibility = View.GONE
        mView.tv_thirdplace.visibility = View.GONE
        mView.tv_fourthplace.visibility = View.GONE
        mView.tv_fifthplace.visibility = View.GONE
        mView.tv_myScore.visibility = View.VISIBLE

        if (!PreGameRoomActivity.isHost)
            mView.btSbFrgNext.visibility = View.INVISIBLE

        mView.btSbFrgNext.setOnClickListener {
            for (endpointId in PreGameRoomActivity.userEndpoints) {
                val byteArray = "startCounter".toByteArray(AndroidNearby.UTF_8)
                AndroidNearby.connectionsClient!!.sendPayload(
                        endpointId,
                        Payload.fromBytes(byteArray)
                )
                Log.d(TAG, "setOnClickListener: $endpointId")
            }
            CountdownTimerTask().execute()
        }

        setupScores()
        mView.tv_myScore.setTextForCast(myScore,context!!)

        callback = activity as GameCallback
        scCallback = this
        return mView
    }

    @SuppressLint("StaticFieldLeak")
    inner class ScoreboardInit : AsyncTask<String, Unit, Unit>() {
        override fun doInBackground(vararg params: String?) {
            Utils.stripScoreboardFromFB(params[0]!!)
            Log.d("DOWNLOAD ASYNC TASK", "DONE")
        }
    }

    override fun scoreboardViewInit(pin: String) {
        ScoreboardInit().execute(pin)
    }

    inner class CountdownTimerTask: AsyncTask<Void, Int, Int>() {
        override fun onPreExecute() {
            super.onPreExecute()
            tvFrgSbTimer.text = Constants.MAX_SECONDS.toString()
        }

        override fun doInBackground(vararg params: Void?): Int {
            for (i in 10 downTo 1) {
                publishProgress(i)
                try {
                    Thread.sleep(1000)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            return 1
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            tvFrgSbTimer.text = values[0].toString()
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            nearbyConfig()
        }
    }

    private fun setupScores() {
        val params1 = mView.tv_firstplace.layoutParams as LinearLayout.LayoutParams
        val params2 = mView.tv_secondplace.layoutParams as LinearLayout.LayoutParams
        val params3 = mView.tv_thirdplace.layoutParams as LinearLayout.LayoutParams
        val params4 = mView.tv_fourthplace.layoutParams as LinearLayout.LayoutParams
        val params5 = mView.tv_fifthplace.layoutParams as LinearLayout.LayoutParams

        when (Utils.allPlayers.size) {
            1 -> {
                mView.tv_firstplace.visibility = View.VISIBLE
                params1.weight = 60.toFloat()
                mView.tv_firstplace.layoutParams = params1
                mView.tv_firstplace.setTextForCast(player1score,context!!)
            }
            2 -> {
                mView.tv_firstplace.visibility = View.VISIBLE
                params1.weight = 20.toFloat()
                mView.tv_firstplace.layoutParams = params1
                mView.tv_firstplace.setTextForCast(player1score,context!!)

                mView.tv_secondplace.visibility = View.VISIBLE
                params2.weight = 20.toFloat()
                mView.tv_secondplace.layoutParams = params2
                mView.tv_secondplace.setTextForCast(player2score,context!!)

                mView.tv_thirdplace.visibility = View.VISIBLE
                params3.weight = 20.toFloat()
                mView.tv_thirdplace.layoutParams = params3
                mView.tv_thirdplace.text = ""

            }
            3 -> {
                mView.tv_firstplace.visibility = View.VISIBLE
                params1.weight = 20.toFloat()
                mView.tv_firstplace.layoutParams = params1
                mView.tv_firstplace.setTextForCast(player1score,context!!)

                mView.tv_secondplace.visibility = View.VISIBLE
                params2.weight = 20.toFloat()
                mView.tv_secondplace.layoutParams = params2
                mView.tv_secondplace.setTextForCast(player2score,context!!)

                mView.tv_thirdplace.visibility = View.VISIBLE
                params3.weight = 20.toFloat()
                mView.tv_thirdplace.layoutParams = params3
                mView.tv_thirdplace.setTextForCast(player3score,context!!)
            }
            4 -> {
                mView.tv_firstplace.visibility = View.VISIBLE
                params1.weight = 15.toFloat()
                mView.tv_firstplace.layoutParams = params1
                mView.tv_firstplace.setTextForCast(player1score,context!!)

                mView.tv_secondplace.visibility = View.VISIBLE
                params2.weight = 15.toFloat()
                mView.tv_secondplace.layoutParams = params2
                mView.tv_secondplace.setTextForCast(player2score,context!!)

                mView.tv_thirdplace.visibility = View.VISIBLE
                params3.weight = 15.toFloat()
                mView.tv_thirdplace.layoutParams = params3
                mView.tv_thirdplace.setTextForCast(player3score,context!!)

                mView.tv_fourthplace.visibility = View.VISIBLE
                params4.weight = 15.toFloat()
                mView.tv_fourthplace.layoutParams = params4
                mView.tv_fourthplace.setTextForCast(player4score,context!!)
            }
            5 -> {
                mView.tv_firstplace.visibility = View.VISIBLE
                params1.weight = 12.toFloat()
                mView.tv_firstplace.layoutParams = params1
                mView.tv_firstplace.setTextForCast(player1score,context!!)

                mView.tv_secondplace.visibility = View.VISIBLE
                params2.weight = 12.toFloat()
                mView.tv_secondplace.layoutParams = params2
                mView.tv_secondplace.setTextForCast(player2score,context!!)

                mView.tv_thirdplace.visibility = View.VISIBLE
                params3.weight = 12.toFloat()
                mView.tv_thirdplace.layoutParams = params3
                mView.tv_thirdplace.setTextForCast(player3score,context!!)

                mView.tv_fourthplace.visibility = View.VISIBLE
                params4.weight = 12.toFloat()
                mView.tv_fourthplace.layoutParams = params4
                mView.tv_fourthplace.setTextForCast(player4score,context!!)

                mView.tv_fifthplace.visibility = View.VISIBLE
                params5.weight = 12.toFloat()
                mView.tv_fifthplace.layoutParams = params5
                mView.tv_fifthplace.setTextForCast(player5score,context!!)
            }
            else -> {}
        }
    }

    private fun nearbyConfig() {
        if (GameFragment.questionNum < localDataQuestions.gameQuestions.size) {
            for (endpointId in PreGameRoomActivity.userEndpoints) {
                val byteArray = "nextQuestion".toByteArray(AndroidNearby.UTF_8)
                AndroidNearby.connectionsClient!!.sendPayload(
                        endpointId,
                        Payload.fromBytes(byteArray)
                )
                Log.d(TAG, "setOnClickListener: $endpointId")
            }
            callback.switchFragment(0)
            if (PreGameRoomActivity.serviceRunning) {
                val switchViewIntent = Intent(context,PresentationService::class.java)
                switchViewIntent.putExtra(Constants.INTENT_MSG_ID, Constants.SWITCH_TO_GAMERROM)
                context!!.startService(Intent(switchViewIntent).setAction(Constants.INTENT_TV_SWITCHVIEW))
            }

        } else if (GameFragment.questionNum == localDataQuestions.gameQuestions.size) {
            for (endpointId in PreGameRoomActivity.userEndpoints) {
                val byteArray = "finalResult".toByteArray(AndroidNearby.UTF_8)
                AndroidNearby.connectionsClient!!.sendPayload(
                        endpointId,
                        Payload.fromBytes(byteArray)
                )
                Log.d(TAG, "setOnClickListener: $endpointId")
            }
            callback.switchFragment(2)
            if (PreGameRoomActivity.serviceRunning) {
                val switchViewIntent = Intent(context,PresentationService::class.java)
                switchViewIntent.putExtra(Constants.INTENT_MSG_ID, Constants.SWITCH_TO_FINALSCORE)
                context!!.startService(Intent(switchViewIntent).setAction(Constants.INTENT_TV_SWITCHVIEW))
            }
        }
    }
}