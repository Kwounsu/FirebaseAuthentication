package com.example.authenticationapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    lateinit var googleSignInClient : GoogleSignInClient
    private val RC_SIGN_IN = 1000
    var callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Email
        button_email.setOnClickListener {
            createEmail()
        }

        // Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        button_google.setOnClickListener {
            val signInIntent = googleSignInClient?.signInIntent
            startActivityForResult(signInIntent,RC_SIGN_IN)
        }

        button_facebook.setOnClickListener {
            facebookLogin()
        }
    }

    fun createEmail(){
        val email = editText_email.text.toString()
        val password = editText_password.text.toString()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                Toast.makeText(this,"Success",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,"Failed",Toast.LENGTH_LONG).show()
            }
        }
    }

    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(acct?.idToken,null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                Toast.makeText(this,"Success",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,"Failed",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun facebookLogin(){
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile","email"))
        LoginManager.getInstance().registerCallback(callbackManager,object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                firebaseAuthWithFacebook(result)
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException?) {
            }

        })
    }
    fun firebaseAuthWithFacebook(result: LoginResult?){
        var credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                Toast.makeText(this,"Success",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,"Failed",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode,resultCode,data)
        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        }
    }
}
