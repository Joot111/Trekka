package pt.ipt.dama2026.trekka.service

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.MainActivity
import pt.ipt.dama2026.trekka.TrekkaApplication
import pt.ipt.dama2026.trekka.data.repository.TrailRepository
import java.util.Locale

class TrackingService : Service(), SensorEventListener {

    private lateinit var fused: FusedLocationProviderClient
    private lateinit var repository: TrailRepository
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentTrailId: Long = -1
    private var pointIndex = 0

    // Métricas em tempo real
    private var totalDistance = 0f
    private var lastLocation: Location? = null
    private var startTime: Long = 0
    private var isMoving = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (currentTrailId == -1L) return
            for (loc in result.locations) {
                // 1. Calcular distância acumulada
                lastLocation?.let {
                    totalDistance += it.distanceTo(loc)
                }
                lastLocation = loc

                // 2. Grava o ponto na DB
                serviceScope.launch {
                    repository.addPoint(currentTrailId, loc.latitude, loc.longitude, pointIndex++)
                }
                
                // 3. Atualizar notificação com a nova distância
                updateNotification()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = (application as TrekkaApplication).repository
        fused = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar Sensores (Requisito: 2º Hardware - Acelerómetro)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        startTime = System.currentTimeMillis()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentTrailId = intent?.getLongExtra("TRAIL_ID", -1) ?: -1

        // Para Android 14+, precisamos de passar o tipo de serviço
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIF_ID, buildNotification())
        }

        startTracking()
        return START_STICKY
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startTracking() {
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()
        fused.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())

        // Registar sensor de movimento
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Lógica para detetar movimento (aceleração resultante sem gravidade simplificada)
            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())
            val newIsMoving = acceleration > 11.5 // Valor acima de 9.8 (gravidade) indica movimento
            
            if (newIsMoving != isMoving) {
                isMoving = newIsMoving
                updateNotification()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        fused.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)

        // SALVAR MÉTRICAS FINAIS NO TRILHO
        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime) / 1000
        val finalDistance = totalDistance.toDouble()
        val trailIdToUpdate = currentTrailId

        serviceScope.launch {
            if (trailIdToUpdate != -1L) {
                repository.updateTrailStats(trailIdToUpdate, finalDistance, duration)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val channelId = "trekka_tracking_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(NotificationChannel(channelId, "Tracking", NotificationManager.IMPORTANCE_LOW))
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusText = if (isMoving) "Em movimento" else "Parado"
        val distanceText = String.format(Locale.getDefault(), "%.2f km", totalDistance / 1000)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Trekka — a gravar trilho")
            .setContentText("Distância: $distanceText | $statusText")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object { private const val NOTIF_ID = 1 }
}
