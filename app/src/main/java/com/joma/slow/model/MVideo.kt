package com.joma.slow.model

import android.graphics.Bitmap
import android.net.Uri

data class MVideo(
    var uri: Uri?,
    var thumb: Bitmap?,
    var duration: String
)