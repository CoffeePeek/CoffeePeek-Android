package org.example.project

import androidx.room.Room
import org.example.project.FPDatabase.Companion.configure

actual class MyRoomRepositoryImpl private constructor(
    myRoomDatabase: FPDatabase
): FPDatabaseRepository(myRoomDatabase){


    companion object {

        private var repo: FPDatabaseRepository? = null

        operator fun invoke(
            dbPath:String
        ): DatabaseCore {
            if (repo == null) synchronized(this){
                if (repo == null) repo = MyRoomRepositoryImpl(
                    Room.databaseBuilder<FPDatabase>(
                        name = dbPath
                    ).configure()
                )
            }
            return repo!!
        }


        fun Database.database(dbPath: String) = invoke(dbPath)

    }



}