package com.example.cattasticpos.domain.model

/**
 * The latest published app build, as advertised by the cloud `app_release` row.
 * Surfaced to the user only when [versionCode] is newer than the running build.
 */
data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val notes: String?,
    val mandatory: Boolean
)
