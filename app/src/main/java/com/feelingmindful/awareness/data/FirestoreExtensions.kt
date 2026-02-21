package com.feelingmindful.awareness.data

import com.google.firebase.Timestamp
import kotlinx.datetime.Instant

internal fun Timestamp.toKotlinxInstant(): Instant =
    Instant.fromEpochSeconds(seconds, nanoseconds)

internal fun Instant.toTimestamp(): Timestamp =
    Timestamp(epochSeconds, nanosecondsOfSecond)
