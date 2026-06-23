package pt.ipt.dama2026.trekka.service

import android.Manifest
import android.app.*
import android.content.*
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

class TrackingService : Service() {

    private lateinit var fused: FusedLocationProviderClient
    private lateinit var repository: TrailRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentTrailId: Long = -1
    private var pointIndex = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (currentTrailId == -1L) return
            for (loc in result.locations) {
                // Grava o ponto na DB usando o Repositório
                serviceScope.launch {
                    repository.addPoint(currentTrailId, loc.latitude, loc.longitude, pointIndex++)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = (application as TrekkaApplication).repository
        fused = LocationServices.getFusedLocationProviderClient(this)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentTrailId = intent?.getLongExtra("TRAIL_ID", -1) ?: -1
        startForeground(NOTIF_ID, buildNotification())
        startTracking()
        return START_STICKY
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startTracking() {
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()
        fused.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())
    }

    override fun onDestroy() {
        super.onDestroy()
        fused.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

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

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Trekka — a gravar trilho")
            .setContentText("A capturar a tua localização...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object { private const val NOTIF_ID = 1 }
}