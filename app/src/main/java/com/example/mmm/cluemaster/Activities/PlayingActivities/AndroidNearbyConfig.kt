package com.example.mmm.cluemaster.Activities.PlayingActivities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.mmm.cluemaster.Activities.HomeActivity
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.GameFragment
import com.example.mmm.cluemaster.Activities.PlayingActivities.fragments.ScoreboardFragment
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.PresentationService
import com.example.mmm.cluemaster.Utils
import com.example.mmm.cluemaster.Utils.databasePinREF
import com.google.android.gms.nearby.connection.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.nio.charset.Charset

@SuppressLint("StaticFieldLeak")
object AndroidNearby {

    const val TAG: String = "AndroidNearbyClass"
    lateinit var context: Context
    lateinit var gmCallback: GameFragmentCallback
    var connectionsClient: ConnectionsClient? = null
    lateinit var packageName: String
    val UTF_8 = Charset.forName("UTF-8")!!
    var counter: Int = 0

    // Called in Advertiser when user is about to connect
    val mConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated: START")
            connectionsClient!!.acceptConnection(endpointId, mPayloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> { // We're connected! Can now start sending and receiving data.
                    Toast.makeText(context, "Connection Status OK", Toast.LENGTH_SHORT).show()
                    PreGameRoomActivity.userEndpoints.add(endpointId)
                    databasePinREF.child(PreGameRoomActivity.pin).child(Constants.NODE_ALLPLAYERS).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            if (PreGameRoomActivity.userEndpoints.count().toLong() == p0.childrenCount){
                                Toast.makeText(context, "EveryoneConnected!!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })


                    connectionsClient!!.stopDiscovery()
                    Log.d(TAG, "onConnectionResult: STATUS_OK")
                    Log.d(TAG, "onConnectionResult: EndpointId $endpointId")
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(PreGameRoomActivity.TAG, "onConnectionResult: STATUS_CONNECTION_REJECTED")
                } // The connection was rejected by one or both sides.
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.e(PreGameRoomActivity.TAG, "onConnectionResult: STATUS_ERROR")
                } // The connection broke before it was able to be accepted.
            }
        }

        override fun onDisconnected(endpointId: String) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            Log.d(PreGameRoomActivity.TAG, "onDisconnected: Connection Disconnected")
            Toast.makeText(context, "onDisconnect", Toast.LENGTH_SHORT).show()
            PreGameRoomActivity.userEndpoints.clear()
        }
    }

    // Called when user Discovers HOST
    val mEndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.i(PreGameRoomActivity.TAG, "onEndpointFound: endpoint found, connecting")
            connectionsClient!!.requestConnection(packageName, endpointId, mConnectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            // A previously discovered endpoint has gone away.
            Log.i(PreGameRoomActivity.TAG, "onEndpointFound: endpoint lost")
            connectionsClient!!.requestConnection(packageName, endpointId, mConnectionLifecycleCallback)
        }
    }

    // Callbacks for receiving payloads
    val mPayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d(TAG, "onPayloadReceived: STARTED")

            val e: ByteArray = payload.asBytes()!!
            val payloadResult = String(e, UTF_8)

            when (payloadResult) {
                "startGame" -> {
                    Log.d(TAG, "onPayloadReceived: startGame")
                    val intent = Intent(context, GameRoomActivity::class.java)
                    intent.putExtra(Constants.INTENT_MSG_PIN, PreGameRoomActivity.pin)
                    context.startActivity(intent)
                }
                "disconnect" -> {
                    val intent = Intent(context, HomeActivity::class.java)
                    context.startActivity(intent)

                    if (PreGameRoomActivity.isHost) {
                        connectionsClient!!.stopAdvertising()
                        Log.d(TAG, "onPayloadReceived: Host stops Advertising")
                    } else {
                        connectionsClient!!.stopDiscovery()
                        Log.d(TAG, "onPayloadReceived: User stops Discovering")
                    }
                }
                "nextQuestion" -> { }
                "startScoreboard" -> {
                    ScoreboardFragment.callback.switchFragment(1)
                    if (PreGameRoomActivity.serviceRunning) {
                        val switchViewIntent = Intent(context, PresentationService::class.java)
                        switchViewIntent.putExtra(Constants.INTENT_MSG_ID, Constants.SWITCH_TO_SCOREBOARD)
                        context.startService(Intent(switchViewIntent).setAction(Constants.INTENT_TV_SWITCHVIEW))
                    }

                }
                "finalResult" -> {
                    ScoreboardFragment.callback.switchFragment(2)
                    if (PreGameRoomActivity.serviceRunning) {
                        val switchViewIntent = Intent(context,PresentationService::class.java)
                        switchViewIntent.putExtra(Constants.INTENT_MSG_ID, Constants.SWITCH_TO_FINALSCORE)
                        context.startService(Intent(switchViewIntent).setAction(Constants.INTENT_TV_SWITCHVIEW))
                    }
                }
                "startCounter" -> {
                    ScoreboardFragment.startTimer()
                }
                "startPbForAudio" -> {
                    GameFragment.playAudioCallback()
                }
            }

            Log.d(PreGameRoomActivity.TAG, "onPayloadReceived: Payload data -> $payloadResult")
            Toast.makeText(context, payloadResult, Toast.LENGTH_SHORT).show()
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.d(PreGameRoomActivity.TAG, "onPayloadTransferUpdate: START")
        }
    }
}