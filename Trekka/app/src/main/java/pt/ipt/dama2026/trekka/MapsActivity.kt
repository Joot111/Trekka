package pt.ipt.dama2026.trekka

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var trailId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Receber o ID da trilha vindo do Histórico
        trailId = intent.getLongExtra("TRAIL_ID", -1)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        enableEdgeToEdge() // Se quiseres manter o estilo das outras telas

        val btnBack = findViewById<ImageButton>(R.id.btnBackMap)
        btnBack.setOnClickListener { finish() }

        // Ajustar as margens para o botão não ficar debaixo da barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(btnBack) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as FrameLayout.LayoutParams
            params.topMargin = systemBars.top + 16 // Dá um espaço extra
            v.layoutParams = params
            insets
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        drawTrail()
    }

    private fun drawTrail() {
        val repository = (application as TrekkaApplication).repository
        
        lifecycleScope.launch {
            // Vai buscar os pontos à base de dados
            val points = repository.getPoints(trailId).first()
            
            if (points.isNotEmpty()) {
                val polylineOptions = PolylineOptions()
                    .color(Color.BLUE)
                    .width(10f)
                
                val boundsBuilder = LatLngBounds.Builder()

                for (p in points) {
                    val latLng = LatLng(p.latitude, p.longitude)
                    polylineOptions.add(latLng)
                    boundsBuilder.include(latLng)
                }

                // Desenha a linha no mapa
                mMap.addPolyline(polylineOptions)

                // Adicionar marcadores de Início e Fim
                val first = points.first()
                val last = points.last()
                
                mMap.addMarker(MarkerOptions()
                    .position(LatLng(first.latitude, first.longitude))
                    .title("Início")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

                mMap.addMarker(MarkerOptions()
                    .position(LatLng(last.latitude, last.longitude))
                    .title("Fim")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

                // Ajusta a câmara para mostrar o trilho completo
                val bounds = boundsBuilder.build()
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
        }
    }
}
