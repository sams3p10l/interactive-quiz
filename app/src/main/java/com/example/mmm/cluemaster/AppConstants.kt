package com.example.mmm.cluemaster

object Constants {

    // Intent extra to switch views for Cast
    const val SWITCH_TO_PREGAME = 0
    const val SWITCH_TO_GAMERROM = 1
    const val SWITCH_TO_SCOREBOARD = 2
    const val SWITCH_TO_FINALSCORE = 3

    // Intent messages
    const val INTENT_MSG_ID = "id"
    const val INTENT_MSG_PIN = "PIN"
    const val INTENT_MSG_NAME = "name"
    const val INTENT_MSG_DESC = "desc"
    const val INTENT_MSG_QUESTION_TITLE = "q_title"
    const val INTENT_MSG_QUESTION_ANS1 = "ans1"
    const val INTENT_MSG_QUESTION_ANS2 = "ans2"
    const val INTENT_MSG_QUESTION_ANS3 = "ans3"
    const val INTENT_MSG_QUESTION_ANS4 = "ans4"
    const val INTENT_MSG_QUESTION_CORRECT_POS = "correct_position"
    const val INTENT_MSG_QUESTION_FILE_PATH = "file_path"


    // TV Cast Actions that start services
    const val INTENT_TV_ACTION = "txtUpdate"
    const val INTENT_TV_INFLATE = "inflatePlayers"
    const val INTENT_TV_SWITCHVIEW = "switchView"
    const val INTENT_TV_UPDATEANS = "btnUpdate"
    const val INTENT_TV_PB = "pbStart"
    const val INTENT_TV_SETUPMEDIA = "setUpMedia"
    const val INTENT_TV_PLAYVIDEO = "playVideo"

    // Firebase Nodes
    const val NODE_ALLPLAYERS = "allPlayers"
    const val NODE_ACTIVEGAMES = "activeGames"
    const val NODE_USERS = "users"
    const val NODE_QUESTIONLIST = "questionsList"
    const val NODE_QUIZZESLIST = "quizzesList"
    const val NODE_SCOREBOARDS = "scoreBoards"
    const val NODE_MYSCORES = "myScores"

    //Bundle keys
    const val BUNDLE_PIN2FRAG = "pin2frag"

    //Game consts
    const val MAX_SECONDS = 10
    const val PTS_MULTIPLIER = 100
    const val APP_ID = "F7216D40"
}