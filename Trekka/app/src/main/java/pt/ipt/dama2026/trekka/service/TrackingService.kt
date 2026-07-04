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
import androidx.core.content.edit
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.MainActivity
import pt.ipt.dama2026.trekka.TrekkaApplication
import pt.ipt.dama2026.trekka.data.repository.TrailRepository
import java.util.Locale

/**
 * Serviço de Foreground responsável pela recolha de dados de localização e sensores.
 * Cumpre os requisitos de hardware utilizando GPS e Acelerómetro (Deteção de Movimento).
 * Funciona em background para garantir que a gravação não é interrompida pelo sistema.
 */
class TrackingService : Service(), SensorEventListener {

    private lateinit var fused: FusedLocationProviderClient
    private lateinit var repository: TrailRepository
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentTrailId: Long = -1
    private var pointIndex = 0

    // Métricas em tempo real acumuladas durante a sessão
    private var totalDistance = 0f
    private var lastLocation: Location? = null
    private var startTime: Long = 0
    private var isMoving = false

    /**
     * Callback invocado periodicamente pelo Google Play Services com novas localizações.
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (currentTrailId == -1L) return
            for (loc in result.locations) {
                // Cálculo da distância incremental
                lastLocation?.let {
                    totalDistance += it.distanceTo(loc)
                }
                lastLocation = loc

                // Persistência assíncrona do ponto na base de dados Room
                serviceScope.launch {
                    repository.addPoint(currentTrailId, loc.latitude, loc.longitude, pointIndex++)
                }
                
                // Atualização da notificação visível para o utilizador
                updateNotification()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = (application as TrekkaApplication).repository
        fused = LocationServices.getFusedLocationProviderClient(this)

        // Inicialização do Acelerómetro (2º Componente de Hardware exigido)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        startTime = System.currentTimeMillis()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentTrailId = intent?.getLongExtra("TRAIL_ID", -1) ?: -1
        
        // Mantém o ID ativo para recuperação caso a app seja fechada
        getSharedPreferences("trekka_prefs", MODE_PRIVATE).edit {
            putLong("active_trail_id", currentTrailId)
        }

        // Conformidade com Android 14 (U): Especificar FOREGROUND_SERVICE_TYPE_LOCATION
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIF_ID, buildNotification())
        }

        startTracking()
        return START_STICKY // Garante que o serviço tenta reiniciar se for morto pelo sistema
    }

    /**
     * Inicia as atualizações de GPS e o escuta do sensor.
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startTracking() {
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()
        fused.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * Processa dados do acelerómetro para detetar se o utilizador está em movimento ou parado.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Cálculo da magnitude da aceleração resultante
            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())
            val newIsMoving = acceleration > 11.5 // Sensibilidade ajustada para deteção de passos/marcha
            
            if (newIsMoving != isMoving) {
                isMoving = newIsMoving
                updateNotification() // Reflete o estado na UI da notificação
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Invocado ao terminar a gravação. Salva as estatísticas finais (distância e duração).
     */
    override fun onDestroy() {
        super.onDestroy()
        fused.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)

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
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification())
    }

    /**
     * Cria e mantém a notificação persistente necessária para serviços de Foreground.
     */
    private fun buildNotification(): Notification {
        val channelId = "trekka_tracking_channel"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
