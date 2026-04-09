package com.rainlean.core

object MotionTuning {
    // Flat detection hysteresis to avoid rapid toggling near threshold.
    const val FLAT_ENTER_DEG = 10.0
    const val FLAT_EXIT_DEG = 16.0

    // Shake detection for linear acceleration magnitude (m/s^2).
    const val SHAKE_TRIGGER_MPS2 = 9.5
    const val SHAKE_SPIKE_MULTIPLIER = 1.35
    const val SHAKE_HOLD_MS = 650L

    // Sensor smoothing and dispatch cadence.
    const val ORIENTATION_ALPHA = 0.18
    const val POSE_DISPATCH_MIN_FRAME_NANOS = 33_000_000L
    const val ACCEL_SMOOTHING_ALPHA = 0.25

    // GLB model local-axis correction so the canopy top aligns with +Y and the handle points -Y.
    // 180° flips the imported umbrella mesh to the correct orientation on screen.
    const val MODEL_YAW_CORRECTION_DEG = 180f
}

