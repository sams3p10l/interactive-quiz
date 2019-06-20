package com.example.mmm.cluemaster.Activities.QuestionsActivities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.Question
import com.example.mmm.cluemaster.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_editquestion.*

class EditQuestionActivity : AppCompatActivity() {

    private val FILE: Int = 0
    var uri: Uri? = null
    private val TAG = "EditQuestionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editquestion)

        val intentToGetExtra = intent

        var title: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_QUESTION_TITLE)
        var ans1: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_QUESTION_ANS1)
        var ans2: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_QUESTION_ANS2)
        var ans3: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_QUESTION_ANS3)
        var ans4: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_QUESTION_ANS4)
        val answer: Int = intentToGetExtra.getIntExtra(Constants.INTENT_MSG_QUESTION_CORRECT_POS, 0)
        val id: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_ID)
        val filePath = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_QUESTION_FILE_PATH)

        var parentID: String

        etQ_t.text = title
        etAns1.setText(ans1)
        etAns2.setText(ans2)
        etAns3.setText(ans3)
        etAns4.setText(ans4)
        etFilePath.setText(filePath)

        // Set radio buttons
        radio_ans1.text = ans1
        radio_ans2.text = ans2
        radio_ans3.text = ans3
        radio_ans4.text = ans4

        // Set correct answer
        when (answer) {
            0 -> radio_ans1.isChecked = true
            1 -> radio_ans2.isChecked = true
            2 -> radio_ans3.isChecked = true
            3 -> radio_ans4.isChecked = true
        }

        // Handle file changes
        ivAddQuestionAddFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, FILE)
        }

        btSaveQuestion.setOnClickListener {
            title = etQ_t.text.toString()
            ans1 = etAns1.text.toString()
            ans2 = etAns2.text.toString()
            ans3 = etAns3.text.toString()
            ans4 = etAns4.text.toString()
            val filePath = etFilePath.text.toString()

            // Check for correct answer
            val correctAnswerEdited: Int = when {
                radio_ans1.isChecked -> 0
                radio_ans2.isChecked -> 1
                radio_ans3.isChecked -> 2
                radio_ans4.isChecked -> 3
                else -> 0
            }

            // Upload File if Needed
            if (uri != null) {
                val mReference = MainActivity.mStorageRef.child(uri!!.lastPathSegment)
                Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

                try {
                    mReference.putFile(uri!!).addOnSuccessListener {
                        mReference.metadata.addOnSuccessListener { storageMetadata ->
                            Log.d(TAG, "setOnClickListener: mediaID -> ${storageMetadata.name}")

                            val questionEdited = Question(id, title, arrayListOf(ans1, ans2, ans3, ans4), storageMetadata.name!!, correctAnswerEdited)
                            val questionsRef = MainActivity.database.getReference(Constants.NODE_QUESTIONLIST)

                            questionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for1@ for (quizSnapshot: DataSnapshot in dataSnapshot.children) {
                                        for (questionSnapshot: DataSnapshot in quizSnapshot.children) {
                                            val question: Question = questionSnapshot.getValue(Question::class.java)!!
                                            if (question.id == id) {
                                                questionSnapshot.ref.parent!!.child(id).setValue(questionEdited)
                                                parentID = questionSnapshot.ref.parent!!.key.toString()
                                                pozivanjeIntenta(parentID)
                                                break@for1
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                        }
                        Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Can't upload file to Firebase", Toast.LENGTH_LONG).show()
                }
            } else {
                val questionEdited = Question(id, title, arrayListOf(ans1, ans2, ans3, ans4), etFilePath.text.toString(), -2)
                val questionsRef = MainActivity.database.getReference(Constants.NODE_QUESTIONLIST)

                questionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for1@ for (quizSnapshot: DataSnapshot in dataSnapshot.children) {
                            for (questionSnapshot: DataSnapshot in quizSnapshot.children) {
                                val question: Question = questionSnapshot.getValue(Question::class.java)!!
                                if (question.id == id) {
                                    questionSnapshot.ref.parent!!.child(id).setValue(questionEdited)
                                    parentID = questionSnapshot.ref.parent!!.key.toString()
                                    pozivanjeIntenta(parentID)
                                    break@for1
                                }
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
        }

        // Handle real-time data binding
        etAns1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // you can call or do what you want with your EditText here
                // yourEditText...
                radio_ans1.text = s
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        etAns2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // you can call or do what you want with your EditText here
                // yourEditText...
                radio_ans2.text = s
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        etAns3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // you can call or do what you want with your EditText here
                // yourEditText...
                radio_ans3.text = s
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        etAns4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // you can call or do what you want with your EditText here
                // yourEditText...
                radio_ans4.text = s
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                FILE -> {
                    uri = data!!.data
                    Log.d(TAG, "onActivityResult: File URI -> $uri")
                    etFilePath.setText(uri.toString())
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Fixed variable value problem
    fun pozivanjeIntenta(parentId: String) {
        Toast.makeText(this, "Updated User", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, QuestionsActivity::class.java)
        intent.putExtra(Constants.INTENT_MSG_ID, parentId)
        startActivity(intent)
    }
}