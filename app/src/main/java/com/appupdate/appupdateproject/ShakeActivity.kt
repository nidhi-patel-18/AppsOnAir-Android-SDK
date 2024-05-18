package com.appupdate.appupdateproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.appsonair.AppsOnAirServices
import com.appsonair.ShakeBugService
import com.appsonair.UpdateCallBack

class ShakeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shake)

        //Get your appId from https://appsonair.com/
        AppsOnAirServices.setAppId("---------fbb66f89-82de-41aa-b6b4-3f18d805583c", true)

        ShakeBugService.shakeBug(this)

        AppsOnAirServices.checkForAppUpdate(this, object : UpdateCallBack {
            override fun onSuccess(response: String) {
                Log.e("mye", "" + response)
            }

            override fun onFailure(message: String) {
                Log.e("mye", "onFailure$message")
            }
        })
    }
}