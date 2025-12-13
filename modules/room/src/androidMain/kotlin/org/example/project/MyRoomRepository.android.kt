package org.example.project

import android.content.Context
import androidx.room.Room
import org.example.project.FPDatabase.Companion.configure

actual class MyRoomRepositoryImpl private constructor(
    myRoomDatabase: FPDatabase
) : FPDatabaseRepository(myRoomDatabase) {


    companion object {

        private var repo: FPDatabaseRepository? = null

        operator fun invoke(
            context: Context,
            databasePath: String
        ): DatabaseCore {
            if (repo == null) synchronized(this) {
                if (repo == null) repo = MyRoomRepositoryImpl(
                    Room.databaseBuilder<FPDatabase>(
                        context = context,
                        name = databasePath
                    ).configure()
                )
            }
            return repo!!
        }

        fun Database.database(
            context: Context,
            databasePath: String
        ) = invoke(context, databasePath)


    }


}