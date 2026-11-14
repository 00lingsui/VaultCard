package com.example.cardmanager.core.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View

object FeedbackManager {
    private const val PREFS_NAME = "vaultcard_feedback_prefs"
    private const val KEY_FEEDBACK_ENABLED = "feedback_enabled"

    @Volatile
    private var cachedEnabled: Boolean? = null

    fun isEnabled(context: Context): Boolean {
        cachedEnabled?.let { return it }
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FEEDBACK_ENABLED, true).also { cachedEnabled = it }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        cachedEnabled = enabled
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FEEDBACK_ENABLED, enabled).apply()
    }

    fun performClick(view: View) {
        if (!isEnabled(view.context)) return
        runCatching {
            val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                HapticFeedbackConstants.CONTEXT_CLICK
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
            view.performHapticFeedback(constant)
        }
        runCatching {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    fun performToggle(view: View) {
        if (!isEnabled(view.context)) return
        runCatching {
            val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                HapticFeedbackConstants.TOGGLE_ON
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
            view.performHapticFeedback(constant)
        }
        runCatching {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    fun performConfirm(view: View) {
        if (!isEnabled(view.context)) return
        runCatching {
            val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
            view.performHapticFeedback(constant)
        }
        runCatching {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    fun performDestructive(view: View) {
        if (!isEnabled(view.context)) return
        val hapticSucceeded = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }.getOrDefault(false)

        if (!hapticSucceeded) {
            vibrateShort(view.context, 28L)
        } else {
            vibrateShort(view.context, 22L)
        }
        runCatching {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateShort(context: Context, durationMs: Long) {
        runCatching {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                mgr?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(durationMs)
                }
            }
        }
    }
}
