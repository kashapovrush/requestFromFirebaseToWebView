package com.template

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessagingService
import java.util.*

class LoadingActivity() : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseMessagingService: FirebaseMessagingService
    private lateinit var database: DatabaseReference
    lateinit var domenFromFirebase: String
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = application.getSharedPreferences("CheckApp", Context.MODE_PRIVATE)

        val connect: ConnectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val checkNet: NetworkInfo? = connect.activeNetworkInfo
        val isConnect: Boolean = checkNet?.isConnectedOrConnecting == true

        if (isConnect) {
            if (!getBoolean("isCheckApp")) {
                putBoolean("isCheckApp", true)
                database.child("db").child("link")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            domenFromFirebase = snapshot.getValue(String::class.java) ?: String()
                            if (domenFromFirebase == null) {
                                val intent = Intent(this@LoadingActivity, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                val intent = Intent(this@LoadingActivity, WebActivity::class.java)
                                putString("isCheck", getLink())
                                startActivity(intent)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            } else {
                val intent = Intent(this@LoadingActivity, WebActivity::class.java)
                startActivity(intent)
            }
        } else {
            val intent = Intent(this@LoadingActivity, WebActivity::class.java)
            startActivity(intent)
        }
    }

    fun putString(key: String, value: String) {
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putString(key, value)
        edit.apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean(key, value)
        edit.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun getLink(): String {
        val packageid = packageName
        val usserid = UUID.randomUUID().toString()
        val timeZone = TimeZone.getDefault()
        val getz = timeZone.id
        val justCode = "getr=utm_source=google-play&utm_medium=organic"
        val link = domenFromFirebase + "/?" + "packageid=" + packageid +
                "&usserid=" + usserid + "&getz=" + getz + "&" + justCode
        return link
    }
}