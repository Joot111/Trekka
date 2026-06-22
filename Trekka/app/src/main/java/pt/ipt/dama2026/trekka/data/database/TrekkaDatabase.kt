package pt.ipt.dama2026.trekka.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pt.ipt.dama2026.trekka.data.dao.TrailDao
import pt.ipt.dama2026.trekka.data.dao.TrailPointDao
import pt.ipt.dama2026.trekka.data.model.Trail
import pt.ipt.dama2026.trekka.data.model.TrailPoint

/**
 * Classe que representa a base de dados do aplicação
 * @see Trail
 * @see TrailPoint
 * @see TrailDao
 * @see TrailPointDao
 * @see TrekkaDatabase
 */

@Database(entities = [Trail::class, TrailPoint::class], version = 1)
abstract class TrekkaDatabase : RoomDatabase() {

    // Interfaces que definem os métodos de acesso aos dados
    abstract fun trailDao(): TrailDao
    abstract fun trailPointDao(): TrailPointDao

    // Objeto que representa a base de dados
    companion object {
        @Volatile private var INSTANCE: TrekkaDatabase? = null
        fun getInstance(context: Context): TrekkaDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, TrekkaDatabase::class.java, "trekka.db").build().also { INSTANCE = it }
            }
    }
}