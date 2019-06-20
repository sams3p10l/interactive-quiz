package com.example.mmm.cluemaster

import com.google.android.gms.cast.CastPresentation
import com.google.android.gms.cast.CastRemoteDisplayLocalService

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.example.mmm.cluemaster.Activities.MainActivity

import org.apmem.tools.layouts.FlowLayout

import java.util.ArrayList

import com.example.mmm.cluemaster.Constants.INTENT_MSG_ID
import com.example.mmm.cluemaster.Constants.INTENT_TV_ACTION
import com.example.mmm.cluemaster.Constants.INTENT_TV_INFLATE
import com.example.mmm.cluemaster.Constants.INTENT_TV_PB
import com.example.mmm.cluemaster.Constants.INTENT_TV_PLAYVIDEO
import com.example.mmm.cluemaster.Constants.INTENT_TV_SETUPMEDIA
import com.example.mmm.cluemaster.Constants.INTENT_TV_SWITCHVIEW
import com.example.mmm.cluemaster.Constants.INTENT_TV_UPDATEANS
import com.example.mmm.cluemaster.Constants.SWITCH_TO_FINALSCORE
import com.example.mmm.cluemaster.Constants.SWITCH_TO_GAMERROM
import com.example.mmm.cluemaster.Constants.SWITCH_TO_PREGAME
import com.example.mmm.cluemaster.Constants.SWITCH_TO_SCOREBOARD
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cast_pregameroom.*
import kotlinx.android.synthetic.main.tv_gameroom_layout.*
import java.lang.Exception


/**
 * Service to keep the remote display running even when the app goes into the background
 */
class PresentationService : CastRemoteDisplayLocalService() {

    // First screen

    private var mPresentation: CastPresentation? = null
    lateinit var exoPlayer: SimpleExoPlayer
    lateinit var exoPlayerView: SimpleExoPlayerView
    private var mMediaPlayer: MediaPlayer = MediaPlayer()

    // function that is invoked whenever this service is started. It is used for communication between host and TV
    override fun onStartCommand(intent: Intent?, i: Int, i1: Int): Int {
        if (intent!!.action != null) {
            when (intent.action) {
                INTENT_TV_ACTION -> {
                    updateTextViewComponent(intent.getIntExtra(INTENT_MSG_ID, 0), intent.getStringExtra("Value"))
                }
                INTENT_TV_INFLATE -> {
                    groupViewInflate(intent.getStringArrayListExtra("listID"), intent.getIntExtra(INTENT_MSG_ID, 0))
                }
                INTENT_TV_SWITCHVIEW -> {
                    switchToView(intent.getIntExtra(Constants.INTENT_MSG_ID, 0))
                }
                INTENT_TV_UPDATEANS -> {
                    showRightAnswer(intent.getIntExtra(INTENT_MSG_ID, 0), intent.getIntegerArrayListExtra("wrongAns"))
                }
                INTENT_TV_PB -> {
                    updateProgressBar(intent.getIntExtra("progress", 0))
                }
                INTENT_TV_SETUPMEDIA -> {
                    setUpViewOfMedia(intent.getStringExtra("url"))
                }
                INTENT_TV_PLAYVIDEO -> {
                    playMe(intent.getStringExtra("clickPlay"))
                }
                else -> {
                    Log.d("PresentationService", "Recieved action is not recognized!")
                }
            }
        }
        return super.onStartCommand(intent, i, i1)

    }

    // Method for Creating presentation
    override fun onCreatePresentation(display: Display?) {
        dismissPresentation()
        mPresentation = FirstScreenPresentation(this, display!!)
        createPresentation()
    }

    // Method for destroying Presentation
    override fun onDismissPresentation() {
        dismissPresentation()
    }

    // Method for clearing presentation if it stayed in cache in last run
    private fun dismissPresentation() {
        if (mPresentation != null) {
            mPresentation!!.dismiss()
            mPresentation = null
        }
    }

    // Show presentation
    private fun createPresentation() {
        try {
            mPresentation!!.show()
        } catch (ex: WindowManager.InvalidDisplayException) {
            dismissPresentation()
        }
    }

    // Switching views on tv (Controls view flow by setting visibility of views)
    fun switchToView(view: Int) {

        if (view == SWITCH_TO_SCOREBOARD) {
            setBtnBackgroundToDefault()
            mPresentation!!.ivGameImage.visibility = View.INVISIBLE
            mPresentation!!.exoplayer.visibility = View.INVISIBLE
            mPresentation!!.btGameFrgPlayAudio.visibility = View.INVISIBLE
            mPresentation!!.tvGameFrgOptinalDisplay.visibility = View.INVISIBLE
            mPresentation!!.tvFrgGameTitle.visibility = View.VISIBLE
        }

        val v: View = mPresentation!!.castPreGame
        v.visibility = View.GONE

        val v1: View = mPresentation!!.castGame
        v1.visibility = View.GONE

        val v2: View = mPresentation!!.castScore
        v2.visibility = View.GONE

        val v3: View = mPresentation!!.castFinal
        v3.visibility = View.GONE

        when (view) {
            SWITCH_TO_PREGAME -> { v.visibility = View.VISIBLE }
            SWITCH_TO_GAMERROM -> { v1.visibility = View.VISIBLE }
            SWITCH_TO_SCOREBOARD -> { v2.visibility = View.VISIBLE }
            SWITCH_TO_FINALSCORE -> { v3.visibility = View.VISIBLE }
            else -> { Log.d("PresentationService","Wrong value to switch view")}
        }
    }

    // Updating txtViews on TV
    private fun updateTextViewComponent(id: Int, value: String) {
        val textView = mPresentation!!.findViewById<TextView>(id)
        textView.text = value
    }


    // Adds players to FlowLayout when they join
    private fun groupViewInflate(list: ArrayList<String>, id: Int) {
        val inflater = mPresentation!!.layoutInflater
        val flowLayout = mPresentation!!.findViewById<FlowLayout>(id)

        flowLayout.removeAllViews()

        for (player in list) {
            val view = inflater.inflate(R.layout.textview, flowLayout, false)
            val textView = view.findViewById<TextView>(R.id.my_id)
            textView.text = player
            flowLayout.addView(textView)
        }
    }

    // Setting background resources to show which answer is correct and which one wrong
    private fun showRightAnswer(id: Int, list: ArrayList<Int>) {
        mPresentation!!.findViewById<Button>(id).setBackgroundResource(R.drawable.button_right_qustion_background)
        for (wrongOnes: Int in list) {
            mPresentation!!.findViewById<Button>(wrongOnes).setBackgroundResource(R.drawable.button_wrong_question_background)
        }
    }

    // Setting answers buttons to normal, because views are changed only by visibility
    private fun setBtnBackgroundToDefault() {
        mPresentation!!.bt_answer4.setBackgroundResource(R.drawable.button_d_background_xxxhdpi)
        mPresentation!!.bt_answer3.setBackgroundResource(R.drawable.button_c_background_xxxhdpi)
        mPresentation!!.bt_answer2.setBackgroundResource(R.drawable.button_b_background_xxxhdpi)
        mPresentation!!.bt_answer1.setBackgroundResource(R.drawable.button_a_background_xxxhdpi)
    }

    // Update progress bar UI on TV
    private fun updateProgressBar(progress: Int) {
        mPresentation!!.pbFrgGame.progress = progress
    }

    // Sets up ExoPlayer, videoURL is needed URL for download of media and checking type of media
    private fun setupExoPlayer(videoURL: String) {
        exoPlayerView = mPresentation!!.exoplayer as SimpleExoPlayerView

        try {
            val bandwidthMeter = DefaultBandwidthMeter()
            val trackSelector: TrackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))

            exoPlayer = ExoPlayerFactory.newSimpleInstance(applicationContext, trackSelector)

            val videoURI = Uri.parse(videoURL)
            val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer_video")
            val extractFactory = DefaultExtractorsFactory()
            val mediaSource = ExtractorMediaSource(videoURI, dataSourceFactory, extractFactory, null, null)

            exoPlayerView.player = exoPlayer
            exoPlayer.prepare(mediaSource)
            exoPlayer.playWhenReady = true


        } catch (e: Exception) {
            Log.e("PresentationService", "ExoPlayer crashed" + e.toString())
        }
    }

    // After determining which type media file is, continue with showing it
    private fun setUpViewOfMedia(url: String) {
        val ref = MainActivity.mStorageRef.child("/$url")

        if (url != "null") {
            ref.metadata.addOnSuccessListener { storageMetadata ->
                ref.downloadUrl.addOnCompleteListener { taskSnapshot ->
                    when (storageMetadata.contentType) {
                        "audio/mpeg" -> {
                            setupVisibility(storageMetadata.contentType)
                        }
                        "image/jpeg", "image/gif" -> {
                            setupVisibility(storageMetadata.contentType)
                            mPresentation!!.ivGameImage.visibility = View.VISIBLE
                            Picasso.get().load(taskSnapshot.result).into(mPresentation!!.ivGameImage)
                        }
                        "video/mp4" -> {
                            setupVisibility(storageMetadata.contentType)
                            setupExoPlayer(taskSnapshot.result.toString())
                        }
                    }
                }
            }.addOnFailureListener {
            }
        } else {
            setupVisibility("null")
        }

    }

    // Audio play
    private fun playMe(taskSnapshotResult: String) {
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.stop()
            mPresentation!!.btGameFrgPlayAudio.setImageResource(R.drawable.ic_media_play_dark)
        } else {
            mMediaPlayer = MediaPlayer()
            try {
                mMediaPlayer.setDataSource(taskSnapshotResult)
                mMediaPlayer.prepare()
                mMediaPlayer.seekTo(0)
                mMediaPlayer.start()
                mPresentation!!.btGameFrgPlayAudio.setImageResource(R.drawable.ic_media_stop_dark)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    // Depending on which media type, we shown necessary view
    private fun setupVisibility(type: String) {
        when (type) {
            "audio/mpeg" -> {
                mPresentation!!.btGameFrgPlayAudio.visibility = View.VISIBLE
            }
            "image/jpeg" -> {
                mPresentation!!.ivGameImage.visibility = View.VISIBLE
            }
            "video/mp4" -> {
                mPresentation!!.exoplayer.visibility = View.VISIBLE
            }
            "null" -> {
                mPresentation!!.tvGameFrgOptinalDisplay.visibility = View.VISIBLE
                mPresentation!!.tvFrgGameTitle.visibility = View.INVISIBLE
                mPresentation!!.ivGameImage.visibility = View.INVISIBLE
                mPresentation!!.btGameFrgPlayAudio.visibility = View.INVISIBLE
                mPresentation!!.exoplayer.visibility = View.INVISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
    }

    /**
     * The presentation to show on the first screen (the TV).
     *
     *
     * Note that this display may have different metrics from the display on
     * which the main activity is showing so we must be careful to use the
     * presentation's own [Context] whenever we load resources.
     *
     */

    inner class FirstScreenPresentation(context: Context, display: Display) : CastPresentation(context, display) {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.cast_pregameroom)
            switchToView(SWITCH_TO_PREGAME)
        }
    }
}
