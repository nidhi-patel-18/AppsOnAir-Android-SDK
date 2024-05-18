package com.appsonair

import android.content.Context
import android.graphics.Color

class ShakeBugService {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun shakeBug(
            context: Context,
            pageBackgroundColor: String = "#E8F1FF",
            appBarBackgroundColor: String = "#E8F1FF",
            appBarTitle: String = "New Ticket",
            appBarTitleColor: String = "#000000"
        ) {
            AppsOnAirServices.shakeBug(context)
        }
    }
}
