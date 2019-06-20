package com.example.mmm.cluemaster.Activities.PlayingActivities

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.MediaRouteActionProvider
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Models.UserModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_pregameroom.*
import kotlin.collections.ArrayList
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import android.widget.TextView
import android.support.v7.media.MediaRouteSelector
import android.support.v7.media.MediaRouter
import com.google.android.gms.cast.CastMediaControlIntent
import android.view.MenuItem
import com.example.mmm.cluemaster.*
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.CastRemoteDisplayLocalService
import com.google.android.gms.common.api.Status
import java.util.*

// Global extension function used to update txtViews on phone and TV
fun TextView.setTextForCast(value: String, context: Context) {
    this.text = value
    if (PreGameRoomActivity.serviceRunning) {
        val txtIntent = Intent(context, PresentationService::class.java)
        txtIntent.putExtra(Constants.INTENT_MSG_ID, this.id)
        txtIntent.putExtra("Value", value)
        context.startService(Intent(txtIntent).setAction(Constants.INTENT_TV_ACTION))
    }
}

class PreGameRoomActivity : AppCompatActivity() {

    companion object {
        val STRATEGY = Strategy.P2P_STAR!!
        const val TAG = "PreGameRoomActivity"
        var userEndpoints: HashSet<String> = HashSet()
        lateinit var pin: String
        var isHost: Boolean = false
        var serviceRunning = false
    }

    var players: ArrayList<UserModel>? = null
    val databasePinREF = MainActivity.database.reference.child(Constants.NODE_ACTIVEGAMES)
    var mMediaRouter: MediaRouter? = null
    lateinit var mMediaRouteSelector: MediaRouteSelector
    var mSelectedDevice: CastDevice? = null

    var BUL = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMediaRouter = MediaRouter.getInstance(applicationContext)
        mMediaRouteSelector = MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(Constants.APP_ID))
                .build()

        setContentView(R.layout.activity_pregameroom)

        checkPermission()

        // Config Android Nearby
        AndroidNearby.connectionsClient = Nearby.getConnectionsClient(this)
        AndroidNearby.context = this
        AndroidNearby.packageName = packageName

        pin = intent.getStringExtra(Constants.INTENT_MSG_PIN)

        btStartGame.setOnClickListener {
            for (endpointId in userEndpoints) {
                val byteArray = "startGame".toByteArray(AndroidNearby.UTF_8)
                AndroidNearby.connectionsClient!!.sendPayload(
                        endpointId,
                        Payload.fromBytes(byteArray)
                )
                Log.d(TAG, "setOnClickListener: $endpointId")
            }

            if (serviceRunning) {
                val switchViewIntent = Intent(this@PreGameRoomActivity, PresentationService::class.java)
                switchViewIntent.putExtra(Constants.INTENT_MSG_ID, 1)
                startService(Intent(switchViewIntent).setAction(Constants.INTENT_TV_SWITCHVIEW))
            }

            // Stop Accepting connections
            //AndroidNearby.connectionsClient!!.stopAdvertising()

            val intent = Intent(this, GameRoomActivity::class.java)
            intent.putExtra(Constants.INTENT_MSG_PIN, pin)
            startActivity(intent)
        }

        checkAndAdd()
        tv_host_PIN.text = pin
        setUpPlayerListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isHost) {
            menuInflater.inflate(R.menu.pregame_menu, menu)
            val mediaRouteMenuItem: MenuItem = menu.findItem(R.id.media_route_menu_item)
            val mediaRouteActionProvider: MediaRouteActionProvider = MenuItemCompat.getActionProvider(mediaRouteMenuItem) as MediaRouteActionProvider
            mediaRouteActionProvider.routeSelector = mMediaRouteSelector
            return true
        }
        return false
    }


    override fun onStart() {
        super.onStart()
        if (isHost)
            mMediaRouter!!.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)

        Thread {
            Utils.loadQuestionDataFromFirebase(pin)
        }.start()
    }

    override fun onStop() {
        super.onStop()
        mMediaRouter!!.removeCallback(mMediaRouterCallback)
    }

    // Check if joined user is host or players and add them to Firebase accordingly
    private fun checkAndAdd() {
        databasePinREF.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if ((p0.child(pin).child("hostId").value.toString()) == MainActivity.loggedUser.uid) {
                    btStartGame.visibility = View.VISIBLE
                    isHost = true
                    startAdvertising()
                } else {
                    databasePinREF.child(pin).child(Constants.NODE_ALLPLAYERS).child(MainActivity.logUser.UUID.toString()).setValue(MainActivity.logUser)
                    btStartGame.visibility = View.INVISIBLE
                    startDiscovery()
                    Toast.makeText(applicationContext, "Establishing connection with host", Toast.LENGTH_SHORT).show()

                }
            }
        })
    }

    // Sets listeners for add and remove players from PreGameRoom
    private fun setUpPlayerListener() {
        val playerRef = databasePinREF.child(pin)
        var playerSize: String
        val layoutInflater = layoutInflater
        var view: View
        var listPlayersForCAST = arrayListOf<String>()

        playerRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                playerSize = p0.childrenCount.toString()
                tv_activePlayers.setTextForCast(playerSize, this@PreGameRoomActivity)

                var new: UserModel

                insert_point.removeAllViewsInLayout()
                listPlayersForCAST.clear()
                for (player: DataSnapshot in p0.children) {

                    view = layoutInflater.inflate(R.layout.textview, insert_point, false)
                    // In order to get the view we have to use the new view with text_layout in it
                    new = player.getValue(UserModel::class.java)!!

                    val textView = view.findViewById(R.id.my_id) as TextView
                    textView.text = new.nickname.toString()
                    listPlayersForCAST.add(new.nickname.toString())

                    // Add the text view to the parent layout
                    insert_point.addView(textView)
                }
                if (serviceRunning) {
                    val inflatingIntent = Intent(this@PreGameRoomActivity, PresentationService::class.java)
                    inflatingIntent.putExtra("listID", listPlayersForCAST)
                    inflatingIntent.putExtra(Constants.INTENT_MSG_ID, insert_point.id)
                    startService(Intent(inflatingIntent).setAction(Constants.INTENT_TV_INFLATE))
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                playerSize = p0.childrenCount.toString()
                tv_activePlayers.setTextForCast(playerSize, this@PreGameRoomActivity)
                var new: UserModel

                insert_point.removeAllViewsInLayout()

                listPlayersForCAST.clear()
                for (player: DataSnapshot in p0.children) {

                    view = layoutInflater.inflate(R.layout.textview, insert_point, false)
                    new = player.getValue(UserModel::class.java)!!

                    val textView = view.findViewById(R.id.my_id) as TextView
                    textView.text = new.nickname.toString()
                    listPlayersForCAST.add(new.nickname.toString())

                    insert_point.addView(textView)
                }
                if (serviceRunning) {
                    val inflatingIntent = Intent(this@PreGameRoomActivity, PresentationService::class.java)
                    inflatingIntent.putExtra("listID", listPlayersForCAST)
                    inflatingIntent.putExtra(Constants.INTENT_MSG_ID, insert_point.id)
                    startService(Intent(inflatingIntent).setAction(Constants.INTENT_TV_INFLATE))
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                playerSize = p0.childrenCount.toString()
                tv_activePlayers.setTextForCast(playerSize, this@PreGameRoomActivity)
                var new: UserModel

                insert_point.removeAllViewsInLayout()

                listPlayersForCAST.clear()
                for (player: DataSnapshot in p0.children) {

                    view = layoutInflater.inflate(R.layout.textview, insert_point, false)
                    new = player.getValue(UserModel::class.java)!!

                    val textView = view.findViewById(R.id.my_id) as TextView
                    textView.text = new.nickname.toString()
                    listPlayersForCAST.add(new.nickname.toString())

                    insert_point.addView(textView)
                }
                if (serviceRunning) {
                    val inflatingIntent = Intent(this@PreGameRoomActivity, PresentationService::class.java)
                    inflatingIntent.putExtra("listID", listPlayersForCAST)
                    inflatingIntent.putExtra(Constants.INTENT_MSG_ID, insert_point.id)
                    startService(Intent(inflatingIntent).setAction(Constants.INTENT_TV_INFLATE))
                }
            }
        })
    }

    // Override back button from Bottom nav bar
    override fun onBackPressed() {
        super.onBackPressed()

        // Disconnect from all Endpoints
        if (isHost) {
            val byteArray = "disconnect".toByteArray(AndroidNearby.UTF_8)
            for (endpoint in PreGameRoomActivity.userEndpoints) {
                AndroidNearby.connectionsClient!!.sendPayload(
                        endpoint,
                        Payload.fromBytes(byteArray)
                )
            }

            Utils.removeMeFromGameroom(pin, isHost)

            AndroidNearby.connectionsClient!!.stopAdvertising()

        } else {
            // Disconnect from all Endpoints
            for (endpoint in PreGameRoomActivity.userEndpoints) {
                Log.d(PreGameRoomActivity.TAG, "onBackPressed: Disconnect endpoint: $endpoint")
                AndroidNearby.connectionsClient!!.disconnectFromEndpoint(endpoint)
            }

            Utils.removeMeFromGameroom(pin, isHost)

            // In case a Device is not connected
            AndroidNearby.connectionsClient!!.stopDiscovery()
        }
    }

    // Android Nearby Stuff START
    private fun startDiscovery() {
        Log.d(TAG, "startDiscovery: STARTED")
        AndroidNearby.connectionsClient!!.startDiscovery(
                packageName,
                AndroidNearby.mEndpointDiscoveryCallback,
                DiscoveryOptions.Builder().setStrategy(STRATEGY).build())
                .addOnSuccessListener {
                    // We're discovering!
                }
                .addOnFailureListener {
                    // We were unable to start discovering.
                }
    }

    // Host start notifying others
    private fun startAdvertising() {
        Log.d(TAG, "startAdvertising: STARTED")
        AndroidNearby.connectionsClient!!.startAdvertising(
                MainActivity.loggedUser.displayName.toString(),
                packageName,
                AndroidNearby.mConnectionLifecycleCallback,
                AdvertisingOptions.Builder().setStrategy(STRATEGY).build())
                .addOnSuccessListener {
                    // We're advertising!
                }
                .addOnFailureListener {
                    // We were unable to start advertising.
                }
    }
    // Android Nearby Stuff END

    // Permission Check android.permission.ACCESS_COARSE_LOCATION
    private val COARSE_LOCATION = 2289

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), COARSE_LOCATION)
                return
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Coarse location GRANTED", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Cannot get coarse location", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    // Callback for Cast
    private val mMediaRouterCallback = object : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter?, route: MediaRouter.RouteInfo?) {}

        override fun onRouteChanged(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            super.onRouteChanged(router, route)
            if (BUL) {
                if (mMediaRouter!!.selectedRoute != route && BUL) {
                    BUL = false
                    mMediaRouter!!.selectRoute(route!!)
                    Toast.makeText(applicationContext, "IF", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(applicationContext, "onRouteChanged", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onRouteRemoved(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            mMediaRouter!!.routes.remove(route)
        }

        override fun onRouteSelected(router: MediaRouter?, info: MediaRouter.RouteInfo) {
            mSelectedDevice = CastDevice.getFromBundle(info.extras)

            intent = Intent(this@PreGameRoomActivity, PreGameRoomActivity::class.java)
            val notificationPendingIntent: PendingIntent = PendingIntent.getActivity(
                    this@PreGameRoomActivity, 0, intent, 0)

            val settings = CastRemoteDisplayLocalService.NotificationSettings.Builder()
                    .setNotificationPendingIntent(notificationPendingIntent).build()

            CastRemoteDisplayLocalService.startService(
                    applicationContext, PresentationService::class.java, Constants.APP_ID, mSelectedDevice, settings,
                    object : CastRemoteDisplayLocalService.Callbacks {
                        override fun onRemoteDisplaySessionError(p0: Status?) {
                            serviceRunning = false
                        }

                        override fun onRemoteDisplaySessionEnded(p0: CastRemoteDisplayLocalService?) {
                            serviceRunning = false
                        }

                        override fun onServiceCreated(service: CastRemoteDisplayLocalService) {
                            serviceRunning = true
                        }

                        override fun onRemoteDisplaySessionStarted(service: CastRemoteDisplayLocalService) {
                            tv_host_PIN.setTextForCast(pin, this@PreGameRoomActivity)
                        }
                    }
            )
        }

        override fun onRouteUnselected(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
            CastRemoteDisplayLocalService.stopService()
            mSelectedDevice = null
        }
    }
}




