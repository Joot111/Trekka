package pt.ipt.dama2026.trekka

import android.app.Application
import pt.ipt.dama2026.trekka.data.database.TrekkaDatabase
import pt.ipt.dama2026.trekka.data.repository.TrailRepository

class TrekkaApplication : Application() {
    // Instância única da base de dados e do repositório
    val database by lazy { TrekkaDatabase.getInstance(this) }
    val repository by lazy { TrailRepository(database) }
}