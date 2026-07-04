package pt.ipt.dama2026.trekka

import android.app.Application
import pt.ipt.dama2026.trekka.data.database.TrekkaDatabase
import pt.ipt.dama2026.trekka.data.repository.TrailRepository

/**
 * Classe de Aplicação central.
 * Utilizada para inicializar e fornecer instâncias globais (Singletons) da base de dados Room 
 * e do repositório, garantindo um ciclo de vida consistente dos dados em toda a app.
 */
class TrekkaApplication : Application() {
    
    // Inicialização "lazy" (preguiçosa) para poupar recursos no arranque
    val database by lazy { TrekkaDatabase.getInstance(this) }
    val repository by lazy { TrailRepository(database) }
}
