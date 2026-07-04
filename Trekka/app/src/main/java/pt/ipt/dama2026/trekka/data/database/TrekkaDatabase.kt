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
 * Ponto de acesso central para a base de dados Room local.
 * Define as tabelas (entidades), a versão do esquema e as interfaces DAO.
 * Implementa o padrão Singleton para garantir uma única instância de acesso aos dados.
 */
@Database(entities = [Trail::class, TrailPoint::class], version = 3)
abstract class TrekkaDatabase : RoomDatabase() {

    // Interfaces para operações de dados
    abstract fun trailDao(): TrailDao
    abstract fun trailPointDao(): TrailPointDao

    companion object {
        @Volatile private var INSTANCE: TrekkaDatabase? = null

        /**
         * Retorna a instância única da base de dados.
         * Implementa destruição de dados em migrações para simplificação durante o desenvolvimento.
         */
        fun getInstance(context: Context): TrekkaDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, TrekkaDatabase::class.java, "trekka.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
