package com.example.mmm.cluemaster.Activities.PlayingActivities.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Activities.PlayingActivities.*
import com.example.mmm.cluemaster.Activities.PlayingActivities.AndroidNearby
import com.example.mmm.cluemaster.Activities.PlayingActivities.GameCallback
import com.example.mmm.cluemaster.Activities.PlayingActivities.PreGameRoomActivity
import com.example.mmm.cluemaster.Activities.PlayingActivities.PreGameRoomActivity.Companion.isHost
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Constants.INTENT_TV_PB
import com.example.mmm.cluemaster.Constants.INTENT_TV_PLAYVIDEO
import com.example.mmm.cluemaster.Constants.INTENT_TV_SETUPMEDIA
import com.example.mmm.cluemaster.Models.localDataQuestions
import com.example.mmm.cluemaster.PresentationService
import com.example.mmm.cluemaster.R
import com.example.mmm.cluemaster.Utils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.STATE_ENDED
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.gms.nearby.connection.Payload
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.fragment_game.view.*
import java.lang.Exception
import java.util.concurrent.TimeUnit


class GameFragment : Fragment(), GameFragmentCallback {

    lateinit var callback: GameCallback
    private val TAG: String = "GameFragment"
    lateinit var pinFromGRA: String
    lateinit var mView: View
    private var secondsMaxMillis: Long = 0
    private var questionCount = 0
    private var correctAnswer = 0
    private var startTime = 0L
    private var elapsedTime = 0L
    private var roundScore: Int = 0
    private var mMediaPlayer: MediaPlayer = MediaPlayer()
    lateinit var exoPlayer: SimpleExoPlayer
    lateinit var exoPlayerView: SimpleExoPlayerView

    companion object {
        var questionNum: Int = 0

        lateinit var gmCallback: GameFragmentCallback

        fun playAudioCallback()
        {
            gmCallback.playAudio()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidNearby.counter = 0
        questionNum = 0

        pinFromGRA = arguments!!.getString(Constants.BUNDLE_PIN2FRAG)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_game, container, false)
        secondsMaxMillis = TimeUnit.SECONDS.toMillis(Constants.MAX_SECONDS.toLong())
        questionCount = localDataQuestions.gameQuestions.size

        setupBtnListeners(mView)

        if (PreGameRoomActivity.isHost) {
            mView.btFrgGameScoreboard.visibility = View.VISIBLE
            disableClicking(false)
        }

        startTime = System.currentTimeMillis()
        callback = activity as GameCallback
        gmCallback = this

        return mView
    }

    override fun onStart() {
        super.onStart()

        if (localDataQuestions.gameQuestions.size != 0) {
            setupView()
        }
    }

    override fun onResume() {
        super.onResume()
        AndroidNearby.counter = 0
    }

    override fun onStop() {
        super.onStop()

        ivGameImage.visibility = View.INVISIBLE
        exoplayer.visibility = View.INVISIBLE
        btGameFrgPlayAudio.visibility = View.INVISIBLE
        tvGameFrgOptinalDisplay.visibility = View.INVISIBLE
        tvFrgGameTitle.visibility = View.VISIBLE

    }

    private fun setupExoPlayer(videoURL: String) {
        exoPlayerView = mView.exoplayer as SimpleExoPlayerView

        try {
            val bandwidthMeter = DefaultBandwidthMeter()
            val trackSelector: TrackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))

            exoPlayer = ExoPlayerFactory.newSimpleInstance(activity, trackSelector)

            val videoURI = Uri.parse(videoURL)

            val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer_video")
            val extractFactory = DefaultExtractorsFactory()
            val mediaSource = ExtractorMediaSource(videoURI, dataSourceFactory, extractFactory, null, null)

            exoPlayerView.player = exoPlayer
            exoPlayer.prepare(mediaSource)
            exoPlayer.playWhenReady = true

        } catch (e: Exception) {
            Log.e("GameFragment", "exoplayer error" + e.toString())
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class ProgressBarTask : AsyncTask<Void, Int, Int>() {
        override fun onPreExecute() {
            super.onPreExecute()
            pbFrgGame.max = 10
            startTime = System.currentTimeMillis()
            disableClicking(true)
        }

        override fun doInBackground(vararg params: Void?): Int {
            for (i in 10 downTo 0) {
                publishProgress(i)
                if (PreGameRoomActivity.serviceRunning) {
                    val btnIntent = Intent(context, PresentationService::class.java)
                    btnIntent.putExtra("progress",i)
                    context!!.startService(Intent(btnIntent).setAction(INTENT_TV_PB))
                }
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return 1
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            if (pbFrgGame != null)
                pbFrgGame.progress = values[0]!!
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            ScoreboardFragment.callMe2(pinFromGRA)
            displayRightAnswer(correctAnswer)
        }
    }

    private fun setupView() {
        correctAnswer = localDataQuestions.gameQuestions[questionNum].correctPos

        mView.tvFrgGameTitle.setTextForCast(localDataQuestions.gameQuestions[questionNum].q_text,context!!)
        mView.bt_answer1.setTextForCast(localDataQuestions.gameQuestions[questionNum].answers[0],context!!)
        mView.bt_answer2.setTextForCast(localDataQuestions.gameQuestions[questionNum].answers[1],context!!)
        mView.bt_answer3.setTextForCast(localDataQuestions.gameQuestions[questionNum].answers[2],context!!)
        mView.bt_answer4.setTextForCast(localDataQuestions.gameQuestions[questionNum].answers[3],context!!)
        mView.tvGameFrgOptinalDisplay.setTextForCast(localDataQuestions.gameQuestions[questionNum].q_text,context!!)

        // Check for File Type
        Log.d(TAG, "setupView: File URI -> ${localDataQuestions.gameQuestions[questionNum].media}")
        val ref = MainActivity.mStorageRef.child("/${localDataQuestions.gameQuestions[questionNum].media}")

        if (PreGameRoomActivity.serviceRunning) {
            val mediaIntent = Intent(context, PresentationService::class.java)
            mediaIntent.putExtra("url",localDataQuestions.gameQuestions[questionNum].media)
            context!!.startService(Intent(mediaIntent).setAction(INTENT_TV_SETUPMEDIA))
        }


        if (localDataQuestions.gameQuestions[questionNum].media != "null") {
            disableClicking(false)
            ref.metadata.addOnSuccessListener { storageMetadata ->
                ref.downloadUrl.addOnCompleteListener { taskSnapshot ->
                    when (storageMetadata.contentType) {
                        "audio/mpeg" -> {
                            Log.d(TAG, "Counter: $questionNum")
                            setupVisibility(storageMetadata.contentType)
                            var currentPosition = 0

                            btGameFrgPlayAudio.setOnClickListener {

                                if (PreGameRoomActivity.serviceRunning) {
                                    val playVideoIntent = Intent(context, PresentationService::class.java)
                                    playVideoIntent.putExtra("clickPlay",taskSnapshot.result.toString())
                                    context!!.startService(Intent(playVideoIntent).setAction(INTENT_TV_PLAYVIDEO))
                                }

                                if (mMediaPlayer.isPlaying) {   //TODO if hostic kliknuo na play onda da se svima pokrene
                                    currentPosition = mMediaPlayer.currentPosition
                                    mMediaPlayer.stop()

                                    if (isHost) {
                                        // inform players that audio is stopped and pb begins
                                        for (endpointId in PreGameRoomActivity.userEndpoints) {
                                            val byteArray = "startPbForAudio".toByteArray(AndroidNearby.UTF_8)
                                            AndroidNearby.connectionsClient!!.sendPayload(
                                                    endpointId,
                                                    Payload.fromBytes(byteArray)
                                            )
                                            Log.d(TAG, "startPb: $endpointId")
                                        }
                                    }

                                    ProgressBarTask().execute()
                                    disableClicking(true)
                                    btGameFrgPlayAudio.setImageResource(R.drawable.ic_media_play_dark)
                                } else {
                                    mMediaPlayer = MediaPlayer()
                                    try {
                                        mMediaPlayer.setDataSource(taskSnapshot.result.toString())
                                        mMediaPlayer.prepare()
                                        mMediaPlayer.seekTo(currentPosition)
                                        mMediaPlayer.start()
                                        btGameFrgPlayAudio.setImageResource(R.drawable.ic_media_stop_dark)
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                    }
                                }
                            }
                        }
                        "image/jpeg", "image/gif" -> {
                            Log.d(TAG, "Counter: ${taskSnapshot.result}")
                            setupVisibility(storageMetadata.contentType)
                            ivGameImage.visibility = View.VISIBLE
                            Picasso.get().load(taskSnapshot.result).into(ivGameImage)
                            ProgressBarTask().execute()
                            disableClicking(true)
                        }
                        "video/mp4" -> {
                            Log.d(TAG, "Counter: ${taskSnapshot.result}")
                            setupVisibility(storageMetadata.contentType)
                            setupExoPlayer(taskSnapshot.result.toString())

                            exoPlayer.addListener(object : Player.EventListener{
                                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}
                                override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}
                                override fun onPlayerError(error: ExoPlaybackException?) {}
                                override fun onLoadingChanged(isLoading: Boolean) {}
                                override fun onPositionDiscontinuity() {}
                                override fun onRepeatModeChanged(repeatMode: Int) {}
                                override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {}
                                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                                   if (playbackState == Player.STATE_ENDED) {
                                       ProgressBarTask().execute()
                                       disableClicking(true)
                                   }
                                }
                            })
                        }
                    }
//                    ProgressBarTask().execute()
                }
            }.addOnFailureListener {
                Log.e(TAG, "addOnFailureListener: Error getting metadata")
                questionNum++
            }
        } else {
            setupVisibility("null")
            ProgressBarTask().execute()
        }
        questionNum++
    }

    private fun setupVisibility(type: String) {
        when (type) {
            "audio/mpeg" -> {
                if (!isHost){
                    btGameFrgPlayAudio.isClickable = false
                    btGameFrgPlayAudio.visibility = View.INVISIBLE
                } else {
                    btGameFrgPlayAudio.visibility = View.VISIBLE
                }
            }
            "image/jpeg" -> {
                mView.ivGameImage.visibility = View.VISIBLE
            }
            "video/mp4" -> {
                mView.exoplayer.visibility = View.VISIBLE
            }
            "null" -> {
                tvGameFrgOptinalDisplay.text = localDataQuestions.gameQuestions[questionNum].q_text
                tvGameFrgOptinalDisplay.visibility = View.VISIBLE
                tvFrgGameTitle.visibility = View.INVISIBLE
            }
        }
    }

    private fun scoreAlg(elapsed: Long): Int =
            TimeUnit.MILLISECONDS.toSeconds((secondsMaxMillis - elapsed) * Constants.PTS_MULTIPLIER).toInt()

    private fun setupBtnListeners(mView: View) {
        mView.bt_answer1.setOnClickListener {
            handleButton(0)
            mView.bt_answer1.isSelected = true
            filter_check1.setImageResource(R.mipmap.ic_yes)
        }

        mView.bt_answer2.setOnClickListener {
            handleButton(1)
            mView.bt_answer2.isSelected = true
            filter_check2.setImageResource(R.mipmap.ic_yes)
        }

        mView.bt_answer3.setOnClickListener {
            handleButton(2)
            mView.bt_answer3.isSelected = true
            filter_check3.setImageResource(R.mipmap.ic_yes)
        }

        mView.bt_answer4.setOnClickListener {
            handleButton(3)
            mView.bt_answer4.isSelected = true
            filter_check4.setImageResource(R.mipmap.ic_yes)
        }
    }

    private fun handleButton(btnID: Int) {
        disableClicking(false)
        elapsedTime = System.currentTimeMillis() - startTime

        if (elapsedTime < secondsMaxMillis && correctAnswer == btnID) {
            roundScore += scoreAlg(elapsedTime)
        }
        Utils.addPointsToFB(roundScore, pinFromGRA)
        Log.i("Points earned", roundScore.toString())
    }

    private fun displayRightAnswer(btnID: Int) {
        when (btnID) {
            0 -> {
                mView.bt_answer1.setBackgroundForCast(arrayListOf(mView.bt_answer2.id, mView.bt_answer3.id, mView.bt_answer4.id))
            }
            1 -> {
                mView.bt_answer2.setBackgroundForCast(arrayListOf(mView.bt_answer1.id, mView.bt_answer3.id, mView.bt_answer4.id))
            }
            2 -> {
                mView.bt_answer3.setBackgroundForCast(arrayListOf(mView.bt_answer2.id, mView.bt_answer1.id, mView.bt_answer4.id))
            }
            3 -> {
                mView.bt_answer4.setBackgroundForCast(arrayListOf(mView.bt_answer2.id, mView.bt_answer3.id, mView.bt_answer1.id))
            }
            else -> {
                Log.d(TAG, "displayRightAnswer: Current button ID: $btnID")
            }
        }
    }

    private fun Button.setBackgroundForCast(list: ArrayList<Int>){
        mView.findViewById<Button>(this.id).setBackgroundResource(R.drawable.button_right_qustion_background)
        for (wrongOnes: Int in list){
           mView.findViewById<Button>(wrongOnes).setBackgroundResource(R.drawable.button_wrong_question_background)
        }
        if (PreGameRoomActivity.serviceRunning) {
            val btnIntent = Intent(context, PresentationService::class.java)
            btnIntent.putExtra(Constants.INTENT_MSG_ID,this.id)
            btnIntent.putExtra("wrongAns",list)
            context.startService(Intent(btnIntent).setAction(Constants.INTENT_TV_UPDATEANS))
        }
    }

    private fun disableClicking(set : Boolean){
        if (!isHost || (isHost && (set == false))) {
            mView.bt_answer1.isClickable = set
            mView.bt_answer2.isClickable = set
            mView.bt_answer3.isClickable = set
            mView.bt_answer4.isClickable = set
        }
    }

    override fun playAudio() {
        ProgressBarTask().execute()
    }

}