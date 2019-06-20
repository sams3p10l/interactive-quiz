package com.example.mmm.cluemaster

import android.content.Context
import android.content.Intent
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Activities.PlayingActivities.PreGameRoomActivity
import com.example.mmm.cluemaster.Adapters.HostQuizAdapter
import com.example.mmm.cluemaster.Adapters.QuizAdapter
import com.example.mmm.cluemaster.Models.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

object Utils {

    var databasePinREF = MainActivity.database.reference.child(Constants.NODE_ACTIVEGAMES)
    var databaseScoreboardREF = MainActivity.database.reference.child(Constants.NODE_SCOREBOARDS)
    var databaseQuestinsRef = MainActivity.database.reference.child(Constants.NODE_QUESTIONLIST)
    var userRef = MainActivity.database.reference.child(Constants.NODE_USERS)

    var allPlayers = arrayListOf<String>()

    // Populating local quiz list
    fun populateQuizList(adapter: Any) {
        val quizzesRef = MainActivity.database.getReference(Constants.NODE_QUIZZESLIST).child(MainActivity.loggedUser.uid)

        quizzesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                localQuizDatabase.quizzes.clear()
                for (quizSnapshot: DataSnapshot in dataSnapshot.children) {
                    var quiz: Quiz = quizSnapshot.getValue(Quiz::class.java)!!
                    localQuizDatabase.quizzes.add(quiz)
                }
                (adapter as? QuizAdapter)?.notifyDataSetChanged()
                (adapter as? HostQuizAdapter)?.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    // Returns String value for PIN
    private fun generatePIN(): String {
        var rand = Random()
        return (rand.nextInt(9000000) + 1000000).toString()
    }

    // Remove PIN from Firebase a.k.a. removing game
    fun deletePINFromDatabase(pinToDelete: String) {
        databasePinREF.child(pinToDelete).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.ref.removeValue()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    // Gets available PIN for new game
    fun registerPIN(quizID: String, context: Context) {
        var generatedPIN = generatePIN()

        databasePinREF.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                while ((p0.value?.equals(generatedPIN)) == true) {
                    generatedPIN = generatePIN()
                }

                databasePinREF.child(generatedPIN)
                var createGame = ActiveGame(quizID, arrayListOf(), generatedPIN)
                databasePinREF.child(generatedPIN).setValue(createGame)
                val pregameIntent = Intent(context, PreGameRoomActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                pregameIntent.putExtra(Constants.INTENT_MSG_PIN, generatedPIN)
                context.startActivity(pregameIntent)
            }
        })
    }

    // Returns current user as UserModel object
    fun getMeAsUser(): UserModel {
        return UserModel(MainActivity.loggedUser.displayName.toString(), MainActivity.loggedUser.email.toString(), MainActivity.loggedUser.phoneNumber.toString(), MainActivity.loggedUser.uid)
    }

    // Removes player or whole game depending on user
    fun removeMeFromGameroom(pin: String, host: Boolean) {
        if (!host) {
            databasePinREF.child(pin).child(Constants.NODE_ALLPLAYERS).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    for (playerSnapshot: DataSnapshot in p0.children) {
                        if (getMeAsUser().UUID.toString() == playerSnapshot.key) playerSnapshot.ref.removeValue()
                    }
                }
            })

        } else deletePINFromDatabase(pin)
    }

    // Checks if quiz has questions so it would enable register of PIN
    fun checkIfHasQuestions(id: String, context: Context) {
        databaseQuestinsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChildren()) registerPIN(id, context)
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    // Sets logged User as static variable in form of UserModel for later use
    fun getMeFromFB(id: String) {
        userRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                MainActivity.logUser = p0.getValue(UserModel::class.java)!!
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    // Logs points to Firebase after each question in quiz
    fun addPointsToFB(roundScore: Int, pin: String) {
        databaseScoreboardREF.child(pin).child(MainActivity.logUser.nickname.toString()).child("points").setValue(roundScore)
    }

    // Downloads Scoreboard from Firebase and sort it
    fun stripScoreboardFromFB(pin: String) {
        var cnt = 0
        val arrTemp = arrayListOf<Scoreboard>()
        var arrTempSorted: List<Scoreboard>

        localDataScoreboard.scoreboard.clear()

        databaseScoreboardREF.child(pin).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (player in p0.children) {
                    val playerName = player.key.toString()
                    val playerPts = player.child("points").value as Long
                    arrTemp.add(cnt++, Scoreboard(playerName, playerPts))
                }

                arrTempSorted = arrTemp.sortedWith(compareBy<Scoreboard> { -it.points }.thenBy { it.name })
                localDataScoreboard.scoreboard.addAll(arrTempSorted)
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    // Get quiz ID so we can access questions in quiz to call questionFill function
    fun loadQuestionDataFromFirebase(pin: String) {
        val quizIDref = MainActivity.database.reference.child(Constants.NODE_ACTIVEGAMES)
                .child(pin).child("quizID")

        var quizID: String

        quizIDref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                quizID = p0.value.toString()
                questionFill(quizID)
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

    }

    // Loads Questions from Firebase to local storage for faster drawing od questions elements in Quiz
    private fun questionFill(quizID: String) {
        val questionsRef = MainActivity.database.reference.child(Constants.NODE_QUESTIONLIST)
        localDataQuestions.gameQuestions.clear()

        questionsRef.child(quizID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (question: DataSnapshot in p0.children) {
                    val mQuestion = question.getValue(Question::class.java)!!
                    localDataQuestions.gameQuestions.add(mQuestion)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    // On start game inits Scoreboard with all connected players to value 0
    fun scoreboardInit(pin: String) {
        allPlayers = arrayListOf()

        databasePinREF.child(pin).child(Constants.NODE_ALLPLAYERS).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (player: DataSnapshot in p0.children) {
                    allPlayers.add(player.child("nickname").value as String)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        databaseScoreboardREF.child(pin).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (player in allPlayers) {
                    databaseScoreboardREF.child(pin).child(player).child("points").setValue(0)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    // After game every user writes own scores to Firebase
    fun saveScoreToFirebase(uid: String, model: MyScoreModel) {
        val key = MainActivity.databaseRootRef.child(Constants.NODE_MYSCORES).child(uid).push().key
        MainActivity.databaseRootRef.child(Constants.NODE_MYSCORES).child(uid).child(key!!).setValue(model)
    }
}