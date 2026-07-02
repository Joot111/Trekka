package pt.ipt.dama2026.trekka

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var trailId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        trailId = intent.getLongExtra("TRAIL_ID", -1)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<ImageButton>(R.id.btnBackMap).setOnClickListener { finish() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Pequeno atraso para o emulador estabilizar antes de processar dados
        lifecycleScope.launch {
            delay(500)
            drawTrail()
        }
    }

    private suspend fun drawTrail() {
        val repository = (application as TrekkaApplication).repository

        try {
            // 1. Carregar dados em background
            val allPoints = withContext(Dispatchers.IO) {
                repository.getPoints(trailId).first()
            }

            if (allPoints.isEmpty()) return

            // 2. Preparar opções visuais (Background)
            val polylineOptions = PolylineOptions().color(Color.BLUE).width(10f)
            val boundsBuilder = LatLngBounds.Builder()

            for (p in allPoints) {
                val latLng = LatLng(p.latitude, p.longitude)
                polylineOptions.add(latLng)
                boundsBuilder.include(latLng)
            }

            // 3. Desenhar no Mapa (Main Thread)
            withContext(Dispatchers.Main) {
                mMap.addPolyline(polylineOptions)

                val first = allPoints.first()
                val last = allPoints.last()

                mMap.addMarker(MarkerOptions()
                    .position(LatLng(first.latitude, first.longitude))
                    .title("Início")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

                mMap.addMarker(MarkerOptions()
                    .position(LatLng(last.latitude, last.longitude))
                    .title("Fim")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

                // Mover a câmara sem animações para poupar CPU
                try {
                    val bounds = boundsBuilder.build()
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
                } catch (e: Exception) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 14f))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}