package pt.ipt.dama2026.trekka

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
import pt.ipt.dama2026.trekka.data.api.RetrofitClient
import pt.ipt.dama2026.trekka.data.api.SessionManager

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var trailId: Long = -1
    private var isRemote: Boolean = false
    private var apiTrailId: String? = null
    
    private lateinit var ratingBar: RatingBar
    private lateinit var txtRatingLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        trailId = intent.getLongExtra("TRAIL_ID", -1)
        isRemote = intent.getBooleanExtra("REMOTE_TRAIL", false)
        apiTrailId = intent.getStringExtra("API_TRAIL_ID")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<ImageButton>(R.id.btnBackMap).setOnClickListener { finish() }

        // Configurar UI de Rating
        val cardRating = findViewById<CardView>(R.id.cardRating)
        ratingBar = findViewById(R.id.ratingBar)
        txtRatingLabel = findViewById(R.id.txtRatingLabel)

        if (isRemote && apiTrailId != null) {
            cardRating.visibility = View.VISIBLE
            ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    sendRating(apiTrailId!!, rating)
                }
            }
        } else {
            cardRating.visibility = View.GONE
        }
    }

    private fun sendRating(id: String, rating: Float) {
        val userId = SessionManager(this).fetchUserId() ?: return
        
        lifecycleScope.launch {
            try {
                // Usar a nova classe RateRequest para evitar erros de wildcard do Retrofit
                val request = pt.ipt.dama2026.trekka.data.api.RateRequest(rating.toInt(), userId)
                RetrofitClient.instance.rateTrail(id, request)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapsActivity, R.string.rating_sent, Toast.LENGTH_SHORT).show()
                    
                    // Bloquear após votar com sucesso
                    ratingBar.setIsIndicator(true)
                    txtRatingLabel.setText(R.string.already_rated)
                    setResult(RESULT_OK)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Mostrar o erro amigável agora que a causa técnica foi resolvida
                    Toast.makeText(this@MapsActivity, R.string.rating_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        lifecycleScope.launch {
            delay(500)
            if (isRemote) {
                drawRemoteTrail()
            } else {
                drawLocalTrail()
            }
        }
    }

    private suspend fun drawLocalTrail() {
        val repository = (application as TrekkaApplication).repository
        try {
            val allPoints = withContext(Dispatchers.IO) {
                repository.getPoints(trailId).first()
            }
            if (allPoints.isNotEmpty()) {
                val latLngs = allPoints.map { LatLng(it.latitude, it.longitude) }
                renderPolylineAndMarkers(latLngs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun drawRemoteTrail() {
        val userId = SessionManager(this).fetchUserId()
        apiTrailId?.let { id ->
            try {
                val trailDto = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getTrailById(id)
                }
                
                withContext(Dispatchers.Main) {
                    // Se o utilizador logado já votou neste trilho, bloqueamos as estrelas
                    if (userId != null && trailDto.ratedBy.contains(userId)) {
                        ratingBar.rating = trailDto.rating
                        ratingBar.setIsIndicator(true) // Modo apenas leitura
                        txtRatingLabel.setText(R.string.already_rated)
                    }
                }

                val latLngs = trailDto.points.map { LatLng(it.latitude, it.longitude) }
                renderPolylineAndMarkers(latLngs)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapsActivity, "Erro ao carregar trilho remoto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun renderPolylineAndMarkers(points: List<LatLng>) {
        if (points.isEmpty()) return

        val polylineOptions = PolylineOptions().color(Color.BLUE).width(10f).addAll(points)
        mMap.addPolyline(polylineOptions)

        mMap.addMarker(MarkerOptions()
            .position(points.first())
            .title("Início")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        mMap.addMarker(MarkerOptions()
            .position(points.last())
            .title("Fim")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

        val boundsBuilder = LatLngBounds.Builder()
        points.forEach { boundsBuilder.include(it) }

        try {
            val bounds = boundsBuilder.build()
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
        } catch (e: Exception) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 14f))
        }
    }
}
