package com.example.mmm.cluemaster.Models

data class Scoreboard(var name: String="", var points: Long = 0)

object localDataScoreboard{
    var scoreboard = mutableListOf<Scoreboard>()
}