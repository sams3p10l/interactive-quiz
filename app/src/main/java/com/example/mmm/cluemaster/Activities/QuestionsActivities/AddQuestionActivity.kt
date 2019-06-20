package com.example.mmm.cluemaster.Activities.QuestionsActivities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.Question
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.activity_addquestion.*

class AddQuestionActivity : AppCompatActivity() {

    private var mediaID: String? = null
    private val TAG: String = "AddQuestionActivity"
    private val IMAGE: Int = 0
    private val AUDIO: Int = 1
    private val VIDEO: Int = 2
    var uri: Uri? = null
    val READIMAGE: Int = 253

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addquestion)
        checkPermission()

        val parentID: String = intent.getStringExtra(Constants.INTENT_MSG_ID)

        setupListeners(parentID)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE -> {
                    uri = data!!.data
                }
                AUDIO -> {
                    uri = data!!.data
                }
                VIDEO -> {
                    uri = data!!.data
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READIMAGE)
                return
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            READIMAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Cluemaster can now read your files :)", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied
                    Toast.makeText(applicationContext, "Cannot access your files", Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setupListeners(parentID: String) {
        ivAddQuestionExit.setOnClickListener {
            finish()
        }

        buAddQuestion.setOnClickListener { it ->
            val questionsRef = MainActivity.database.getReference(Constants.NODE_QUESTIONLIST).child(parentID)
            val questionsRefID = questionsRef.push().key

            val lista: ArrayList<String> = arrayListOf()
            lista.add(etAddQuestionAnswer1.text.toString())
            lista.add(etAddQuestionAnswer2.text.toString())
            lista.add(etAddQuestionAnswer3.text.toString())
            lista.add(etAddQuestionAnswer4.text.toString())
            var pos = 0

            if (radioButton1.isChecked)
                pos = 0
            if (radioButton2.isChecked)
                pos = 1
            if (radioButton3.isChecked)
                pos = 2
            if (radioButton4.isChecked)
                pos = 3

            if (uri != null) {
                val mReference = MainActivity.mStorageRef.child(uri!!.lastPathSegment!!)
                Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

                try {
                    mReference.putFile(uri!!).addOnSuccessListener {
                        mReference.metadata.addOnSuccessListener { storageMetadata ->
                            mediaID = storageMetadata.name.toString()

                            Log.d(TAG, "setOnClickListener: mediaID -> $mediaID")
                            val question = Question(questionsRefID.toString(), etConcreteQuestion.text.toString(), lista, mediaID.toString(), pos)
                            questionsRef.child(questionsRefID!!).setValue(question)
                        }
                        Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Can't upload file to Firebase", Toast.LENGTH_LONG).show()
                }
            } else {
                val question = Question(questionsRefID.toString(), etConcreteQuestion.text.toString(), lista, "null", pos)
                questionsRef.child(questionsRefID!!).setValue(question)
            }

            val addQuestionIntent = Intent(this, QuestionsActivity::class.java)
            addQuestionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addQuestionIntent.putExtra(Constants.INTENT_MSG_ID, parentID)

            etConcreteQuestion.text.clear()
            etAddQuestionAnswer1.text.clear()
            etAddQuestionAnswer2.text.clear()
            etAddQuestionAnswer3.text.clear()
            etAddQuestionAnswer4.text.clear()

            startActivity(addQuestionIntent)
        }

        ivAddPicture.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Images"), IMAGE)
        }

        ivAddAudio.setOnClickListener {
            val intent = Intent()
            intent.type = "audio/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Audio"), AUDIO)
        }

        ivAddVideo.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO)
        }
    }
}