package com.example.data.engine

import android.app.ActivityManager
import android.content.Context
import com.example.domain.model.RamTelemetry

object RamTelemetryProvider {
    fun readTelemetry(context: Context): RamTelemetry {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return RamTelemetry()
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return RamTelemetry(
            availableMb = memInfo.availMem / (1024L * 1024L),
            totalMb = memInfo.totalMem / (1024L * 1024L),
            lowMemory = memInfo.lowMemory
        )
    }
}
