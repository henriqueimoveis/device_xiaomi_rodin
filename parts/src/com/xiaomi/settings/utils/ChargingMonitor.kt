/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/** Observes external power for thermal profiles, including while the screen is off. */
class ChargingMonitor(
    private val context: Context,
    private val onChange: (Boolean) -> Unit,
) {
    private var registered = false

    // Keep the charging profile while plugged in, even if charging is paused or full.
    var isCharging = false
        private set

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!registered || intent.action != Intent.ACTION_BATTERY_CHANGED) return
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
            if (plugged == isCharging) return
            isCharging = plugged
            onChange(plugged)
        }
    }

    /** Registers once and initializes the state synchronously from the sticky intent. */
    fun start() {
        if (registered) return
        val battery = context.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            Context.RECEIVER_NOT_EXPORTED,
        )
        registered = true
        isCharging = battery?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)?.let { it != 0 }
            ?: false
    }

    fun stop() {
        if (!registered) return
        registered = false
        context.unregisterReceiver(receiver)
    }
}
