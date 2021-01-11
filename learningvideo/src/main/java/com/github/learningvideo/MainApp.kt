package com.github.learningvideo

import android.app.Application
import android.content.Context

/**
 * Created by lvming on 1/11/21 11:16 AM.
 * Email: lvming@guazi.com
 * Description:application入口
 */

var CONTXT: Context? = null

class MainApp : Application() {
     
    override fun onCreate() {
        super.onCreate()
        CONTXT = this
    }
}