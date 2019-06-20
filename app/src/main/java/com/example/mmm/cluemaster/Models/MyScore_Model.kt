package com.example.mmm.cluemaster.Models

data class MyScoreModel(var date: String, var quizName : String, var score : Long)

object localScoreHistory {var lshObject = arrayListOf<MyScoreModel>() }