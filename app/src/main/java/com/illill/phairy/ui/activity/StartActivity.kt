package com.illill.phairy.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.illill.phairy.BaseApplication.Companion.sharedPreferences
import com.illill.phairy.R
import com.illill.phairy.viewmodel.ClientViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class StartActivity : Activity() {

    private val TAG = StartActivity::class.java.simpleName

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private val clientViewModel by inject<ClientViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        Log.e(TAG, "--StartActivity--")

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
        initUser()
        moveToNext()
    }

    private fun initUser() {

        val uid = sharedPreferences.getString("uid","")
        if(!TextUtils.isEmpty(uid)){

            CoroutineScope(Dispatchers.Main).launch {
                clientViewModel.loadMySelf(uid)
                moveToNext()
            }
            return
        }else{
            moveToNext()
        }
//        val auth = FirebaseAuth.getInstance()
//        val user = auth.currentUser
//        Log.d(TAG, "initUser: user = $user")
//        Log.d(TAG, "initUser: auth= $auth.")
//
//        if (user != null) {
//            CoroutineScope(Dispatchers.Main).launch {
//                clientViewModel.loadMySelf(user.uid)
//                moveToNext()
//            }
//        }else{
//            moveToNext()
//        }
//        else { //Firebase ERROR
//            auth.signInAnonymously()
//                    .addOnCompleteListener(this) { task ->
//                        if (task.isSuccessful) {
//                            // Sign in success, update UI with the signed-in user's information
//                            val uid = auth.currentUser?.uid
//                            Log.d(TAG, "signInAnonymously:success new ${uid}")
//                            if (uid == null) {
//                                Toast.makeText(applicationContext, getString(R.string.error_login), Toast.LENGTH_SHORT).show()
//                                return@addOnCompleteListener
//                            }
//
//                        } else {
//                            Log.w(TAG, "signInAnonymously:failure", task.exception)
//                            Toast.makeText(applicationContext, getString(R.string.error_login), Toast.LENGTH_SHORT).show()
//
//                        }
//                    }
//        }
    }

    private fun moveToNext() = CoroutineScope(Dispatchers.Main).launch {
        withContext(Dispatchers.Default) { delay(1000) }
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

}
