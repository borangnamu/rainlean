package com.rainlean

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.rainlean.core.MotionMath
import com.rainlean.core.MotionTuning
import com.rainlean.presentation.guidance.GuidanceScreen
import com.rainlean.presentation.guidance.GuidanceViewModel
import com.rainlean.ui.theme.RainLeanTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.sqrt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var currentViewModel: GuidanceViewModel? = null
    private var latestHeadingDeg: Double = 0.0
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private var linearAccelSensor: Sensor? = null
    private var latestPitchDeg: Double = 0.0
    private var latestRollDeg: Double = 0.0
    private var latestAzimuthDeg: Double = 0.0
    private var lastPoseDispatchNanos: Long = 0L
    private var shakeHoldUntilMs: Long = 0L
    private var filteredLinearAccelMagnitude: Double = 0.0
    private var flatState: Boolean = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fine = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            refreshWithCurrentLocation(currentViewModel)
        }
    }

    private val headingListener = object : SensorEventListener {
        private val rotationMatrix = FloatArray(9)
        private val orientation = FloatArray(3)
        private var hasHeading = false
        private var hasPose = false

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthRad = orientation[0].toDouble()
            val pitchRad = orientation[1].toDouble()
            val rollRad = orientation[2].toDouble()
            val normalized = ((Math.toDegrees(azimuthRad) + 360.0) % 360.0)
            latestHeadingDeg = if (hasHeading) {
                // Circular interpolation to avoid 359->1 jump artifacts.
                val delta = (((normalized - latestHeadingDeg + 540.0) % 360.0) - 180.0)
                (latestHeadingDeg + delta * 0.15 + 360.0) % 360.0
            } else {
                hasHeading = true
                normalized
            }

            val rawPitchDeg = Math.toDegrees(pitchRad)
            val rawRollDeg = Math.toDegrees(rollRad)
            latestPitchDeg = if (hasPose) {
                MotionMath.lerp(latestPitchDeg, rawPitchDeg, MotionTuning.ORIENTATION_ALPHA)
            } else {
                rawPitchDeg
            }
            latestRollDeg = if (hasPose) {
                MotionMath.lerp(latestRollDeg, rawRollDeg, MotionTuning.ORIENTATION_ALPHA)
            } else {
                rawRollDeg
            }
            latestAzimuthDeg = if (hasPose) {
                MotionMath.circularLerp(latestAzimuthDeg, normalized, MotionTuning.ORIENTATION_ALPHA)
            } else {
                normalized
            }
            hasPose = true

            val nowMs = System.currentTimeMillis()
            val shaking = nowMs < shakeHoldUntilMs
            flatState = MotionMath.updateFlatState(
                currentIsFlat = flatState,
                pitchDeg = latestPitchDeg,
                rollDeg = latestRollDeg,
                enterThresholdDeg = MotionTuning.FLAT_ENTER_DEG,
                exitThresholdDeg = MotionTuning.FLAT_EXIT_DEG
            )
            maybeDispatchPose(
                pitchDeg = latestPitchDeg,
                rollDeg = latestRollDeg,
                azimuthDeg = if (flatState) latestHeadingDeg else latestAzimuthDeg,
                isFlat = flatState,
                isShaking = shaking
            )
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private val linearAccelerationListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()
            val magnitude = sqrt(x * x + y * y + z * z)
            filteredLinearAccelMagnitude = MotionMath.lerp(
                current = filteredLinearAccelMagnitude,
                target = magnitude,
                alpha = MotionTuning.ACCEL_SMOOTHING_ALPHA
            )
            val triggeredByFiltered = filteredLinearAccelMagnitude > MotionTuning.SHAKE_TRIGGER_MPS2
            val triggeredBySpike = magnitude > (MotionTuning.SHAKE_TRIGGER_MPS2 * MotionTuning.SHAKE_SPIKE_MULTIPLIER)
            if (triggeredByFiltered || triggeredBySpike) {
                shakeHoldUntilMs = System.currentTimeMillis() + MotionTuning.SHAKE_HOLD_MS
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        enableEdgeToEdge()
        setContent {
            RainLeanTheme {
                val vm: GuidanceViewModel = hiltViewModel()
                currentViewModel = vm
                LaunchedEffect(Unit) {
                    ensureLocationPermissionThenRefresh(vm)
                }
                GuidanceScreen(
                    viewModel = vm,
                    onRefresh = { refreshWithCurrentLocation(vm) },
                    onForceRefreshForDryWeather = {
                        refreshWithCurrentLocation(vm, forceGuidanceForDryWeather = true)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(
                headingListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        linearAccelSensor?.let {
            sensorManager.registerListener(
                linearAccelerationListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun onStop() {
        super.onStop()
        currentViewModel?.stopAutoRefresh()
    }

    override fun onStart() {
        super.onStart()
        // Re-arm auto-refresh if permission is already granted (e.g. returning from background).
        // Skip if currentViewModel is null — onCreate hasn't run yet on fresh launch.
        val vm = currentViewModel ?: return
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) refreshWithCurrentLocation(vm)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(headingListener)
        sensorManager.unregisterListener(linearAccelerationListener)
    }

    private fun ensureLocationPermissionThenRefresh(viewModel: GuidanceViewModel) {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            refreshWithCurrentLocation(viewModel)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun refreshWithCurrentLocation(
        viewModel: GuidanceViewModel? = null,
        forceGuidanceForDryWeather: Boolean = false
    ) {
        val vm = viewModel ?: return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locationClient.lastLocation.addOnSuccessListener { location ->
            val isLocationFallback = location == null
            val lat = location?.latitude ?: 37.5665
            val lon = location?.longitude ?: 126.9780
            if (forceGuidanceForDryWeather) {
                vm.refresh(
                    lat = lat,
                    lon = lon,
                    userHeadingDeg = latestHeadingDeg,
                    forceGuidanceForDryWeather = true,
                    isLocationFallback = isLocationFallback
                )
            } else {
                vm.startAutoRefresh(
                    lat = lat,
                    lon = lon,
                    isLocationFallback = isLocationFallback,
                    headingProvider = { latestHeadingDeg }
                )
            }
        }.addOnFailureListener {
            if (forceGuidanceForDryWeather) {
                vm.refresh(
                    lat = 37.5665,
                    lon = 126.9780,
                    userHeadingDeg = latestHeadingDeg,
                    forceGuidanceForDryWeather = true,
                    isLocationFallback = true
                )
            } else {
                vm.startAutoRefresh(
                    lat = 37.5665,
                    lon = 126.9780,
                    isLocationFallback = true,
                    headingProvider = { latestHeadingDeg }
                )
            }
        }
    }

    private fun maybeDispatchPose(
        pitchDeg: Double,
        rollDeg: Double,
        azimuthDeg: Double,
        isFlat: Boolean,
        isShaking: Boolean
    ) {
        val vm = currentViewModel ?: return
        val now = System.nanoTime()
        val minimumFrameNanos = MotionTuning.POSE_DISPATCH_MIN_FRAME_NANOS
        if (now - lastPoseDispatchNanos < minimumFrameNanos) return
        lastPoseDispatchNanos = now
        vm.updateDevicePose(
            pitchDeg = pitchDeg,
            rollDeg = rollDeg,
            azimuthDeg = azimuthDeg,
            isFlat = isFlat,
            isShaking = isShaking
        )
    }
}
