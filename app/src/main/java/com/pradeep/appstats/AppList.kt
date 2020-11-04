package com.pradeep.appstats

import android.graphics.drawable.Drawable

data class AppList(
    val name: String,
    var icon: Drawable,
    val packages: String
)