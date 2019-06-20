package com.example.mmm.cluemaster.Models

import com.example.mmm.cluemaster.Activities.MainActivity

data class ActiveGame(var quizID: String="",var allPlayers: ArrayList<UserModel> = arrayListOf(), var scoreBoardId: String = "", var hostId: String = MainActivity.loggedUser.uid)