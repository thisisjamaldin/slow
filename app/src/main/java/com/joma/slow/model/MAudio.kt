package com.joma.slow.model

import android.net.Uri

data class MAudio(
    var uri: Uri?,
    var size: Int,
    var name: String,
    var duration: String,
    var path: String
)