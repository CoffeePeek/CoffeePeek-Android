package org.example.project

import androidx.room.Room
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import org.example.project.MyRoomDatabase.Companion.configure
import org.example.project.databse.Database
import org.example.project.databse.DatabaseCore

actual class MyRoomRepositoryImpl private constructor(
    myRoomDatabase: MyRoomDatabase
): MyRoomRepository(myRoomDatabase){



    companion object {


        private var repo: MyRoomRepository? = null
        private val so = SynchronizedObject()


        operator fun invoke(
           dbPath: String
        ): DatabaseCore {
            if (repo == null) synchronized(so){
                if (repo  == null) repo = MyRoomRepositoryImpl(
                    Room.databaseBuilder<MyRoomDatabase>(
                        name = dbPath
                    ).configure()
                )
            }
            return repo!!
        }

        fun Database.database(dbPath: String) = invoke(dbPath)

    }


}