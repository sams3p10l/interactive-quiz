package com.example.mmm.cluemaster.Activities.AuthActivities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mmm.cluemaster.Activities.HomeActivity
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.UserModel
import com.example.mmm.cluemaster.R
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.android.synthetic.main.activity_login.*
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.twitter.sdk.android.core.*


class LoginActivity : AppCompatActivity() {

    companion object {
        const val RC_SIGN_IN: Int = 1
    }

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private var mCallbackManager: CallbackManager? = null
    private var dbUsersRef = MainActivity.database.reference.child(Constants.NODE_USERS)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Init Twitter Kit
        val config = TwitterConfig.Builder(this)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig(getString(R.string.twitter_CONSUMER_KEY), getString(R.string.twitter_CONSUMER_SECRET)))
                .debug(true)
                .build()
        Twitter.initialize(config)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configure Google Sign In
        ivGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            signInGoogle()
        }

        // Facebook Sign In
        mCallbackManager = CallbackManager.Factory.create()
        ivFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, arrayListOf("email", "public_profile"))
            LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(this@LoginActivity, "facebook:OnCancel:", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@LoginActivity, "facebook:onError:", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Twitter Button Callback
        buTwitterLogin.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                handleTwitterSession(result!!.data)
            }

            override fun failure(exception: TwitterException?) {}
        }

        // Set On Click Listener to redirect a user to Register Activity
        ivEmail.setOnClickListener {
            val intentRegister = Intent(this, RegisterActivity::class.java)
            startActivity(intentRegister)
        }

        // Get FirebaseAuth Ref
        mAuth = MainActivity.mAuth
    }

    fun onLogin(view: View) {
        signInUserWithFirebase(etEmail.text.toString(), etPassword.text.toString())
    }

    private fun signInUserWithFirebase(email: String, password: String) {
        MainActivity.mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        MainActivity.loggedUser = mAuth?.currentUser!!
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // GOOGLE
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
//                MainActivity.loggedUser = mAuth?.currentUser!!
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            // FACEBOOK
            // Pass the activity result back to the Facebook SDK
            mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
            // Pass the activity result to the Twitter login button.
            buTwitterLogin.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Twitter Sign In START
    private fun handleTwitterSession(session: TwitterSession) {
        val credential = TwitterAuthProvider.getCredential(
                session.authToken.token,
                session.authToken.secret)

        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, HomeActivity::class.java)
                        MainActivity.loggedUser = mAuth?.currentUser!!
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Firebase sign in failed(Twitter)", Toast.LENGTH_SHORT).show()
                    }
                }
    }
    // Twitter Sign In END

    // Google Sing In START
    private fun signInGoogle() {
        val signInIntent = mGoogleSignInClient!!.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)

        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Save user to database
                        saveGoogleUserToFirebase(account)
                        MainActivity.loggedUser = mAuth?.currentUser!!
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Firebase sign in failed", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun saveGoogleUserToFirebase(account: GoogleSignInAccount?) {
        val user = mAuth!!.currentUser
        val userId = user!!.uid

        dbUsersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {

                var userNew = UserModel(account!!.familyName, account.email, account.displayName, userId)

                dbUsersRef.child(userId).setValue(userNew)
            }
        })
    }
    // Google Sign In END

    // Facebook Sign in START
    private fun handleFacebookAccessToken(token: AccessToken) {
        Toast.makeText(this, "Firebase Facebook Auth", Toast.LENGTH_SHORT).show()

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, HomeActivity::class.java)
                        MainActivity.loggedUser = mAuth?.currentUser!!
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Firebase sign in failed", Toast.LENGTH_SHORT).show()
                    }
                }
    }
    // Facebook Sign in END
}