package com.illill.phairy.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.illill.phairy.BaseApplication.Companion.sharedPreferences
import com.illill.phairy.BuildConfig
import com.illill.phairy.R
import com.illill.phairy.data.model.Client
import com.illill.phairy.databinding.ActivityLoginBinding
import com.illill.phairy.viewmodel.ClientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : AppCompatActivity(), View.OnClickListener {//}, GoogleApiClient.OnConnectionFailedListener,

    val TAG = LoginActivity::class.java.simpleName

    lateinit var binding: ActivityLoginBinding


    private val facebookLoginButton by lazy { LoginButton(this@LoginActivity) }
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private val clientViewModel: ClientViewModel by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "--LoginActivity--")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

//        checkUser()
        setButtons()
        googleSetting()
        facebookSetting()

    }


    private fun setButtons() {
        binding.clFacebookLogin.setOnClickListener(this)
        binding.clGoogleLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cl_facebook_login -> facebookLoginButton.performClick()
            R.id.cl_google_login -> {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
//            R.id.cl_facebook_login->
        }
    }

    private fun googleSetting() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    private fun facebookSetting() {
        //페북 버튼 콜백 매니져
        callbackManager = CallbackManager.Factory.create()


        facebookLoginButton.setReadPermissions("email")
        facebookLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // App code
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed ${e.statusCode}, ${e.message}")
                Toast.makeText(applicationContext, "에러가 발생했습니다.(${e.statusCode},${e.message})\n에러가 반복되면 문의해주세요", Toast.LENGTH_SHORT).show()
                // ...
            }
        } else callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success ${task.result}")
                        val user = auth.currentUser
                        if (user == null) {
                            Toast.makeText(baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                        sharedPreferences.edit().putString("uid",user.uid).apply()
                        Log.d(TAG, "firebaseAuthWithGoogle: $user")
                        join(user)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)

                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        // ...
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "firebaseAuthWithGoogle: $it")
                    Toast.makeText(baseContext, "에러 : $it" , Toast.LENGTH_SHORT).show();
                }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success ${task.result}")
                        val user = auth.currentUser
                        if (user == null) {
                            Toast.makeText(baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                        Log.d(TAG, "handleFacebookAccessToken: ${user}")
                        sharedPreferences.edit().putString("uid",user.uid).apply()
                        join(user)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",     Toast.LENGTH_SHORT).show()
                    }
                }
    }

    /**
     * anonymous 계정이 아닌 Firebase provider와 연동된 계정이 된 상태.
     * 유저와 받아온 토큰을 이용하여 가입한다.
     */
    private fun join(user: FirebaseUser) {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val _token = task.result?.token

                    val client = Client().apply {
                        uid = user.uid
                        token = _token ?: ""
                        nickname = clientViewModel.getTempNickname(resources)
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        if (clientViewModel.joinOrUpdate(client)) {
                            Toast.makeText(this@LoginActivity, getString(R.string.success_login), Toast.LENGTH_SHORT).show()
                            clientViewModel.loadMySelf(user.uid)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                .addOnFailureListener { e->
                    Toast.makeText(applicationContext, "에러가 발생했습니다.\n에러가 반복되면 문의해주세요.\n:"+e.message, Toast.LENGTH_SHORT).show()
                }


    }





}