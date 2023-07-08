package com.template

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL


class WebActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseMessagingService: FirebaseMessagingService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)


        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf (POST_NOTIFICATIONS),1);
            }
        }
        val webView: WebView = findViewById(R.id.web_view)
        CookieManager.getInstance().setAcceptCookie(true)
        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("CheckApp", Context.MODE_PRIVATE)
        var link = sharedPreferences.getString("isCheck", null)

        val userAgent = webView.settings.userAgentString
        webView.apply {
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.userAgentString = userAgent
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                CookieManager.getInstance().flush()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val intent = Intent(this@WebActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }

        val data: URL? = try {
            URL(link.toString())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            null
        }

        val result = lifecycleScope.async(Dispatchers.IO) {
            try {
                data?.readText()
            } catch (e: IOException) {

            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            webView.loadUrl(result.await().toString())
        }

    }

    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.web_view)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {

        }
    }

}
