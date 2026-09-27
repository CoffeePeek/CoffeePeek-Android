package com.coffeepeek.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@RunWith(AndroidJUnit4::class)
class DatabaseConfigurationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun configuredBuilderKeepsItsIdentityAndSupportsReadWrite(): Unit = runBlocking {
        val builder = Room.inMemoryDatabaseBuilder<FixtureDatabaseV1>(context)
        assertSame(builder, builder.configureDatabaseDriver())
        val database = builder.build()
        try {
            val entry = EntryV1(1, "test value")
            database.entries().insert(entry)
            assertEquals(listOf(entry), database.entries().entries())
        } finally { database.close() }
    }

    @Test
    fun dataSurvivesClosingAndReopening(): Unit = runBlocking {
        val name = "core-database-test-${UUID.randomUUID()}.db"
        try {
            val original = Room.databaseBuilder<FixtureDatabaseV1>(context, name).configureDatabaseDriver().build()
            try { original.entries().insert(EntryV1(1, "persisted")) }
            finally { original.close() }

            val reopened = Room.databaseBuilder<FixtureDatabaseV1>(context, name).configureDatabaseDriver().build()
            try { assertEquals(listOf(EntryV1(1, "persisted")), reopened.entries().entries()) }
            finally { reopened.close() }
        } finally { context.deleteDatabase(name) }
    }

    @Test
    fun callerMigrationPreservesRowsAndAddsDefaultValue(): Unit = runBlocking {
        val name = "core-database-test-${UUID.randomUUID()}.db"
        try {
            val original = Room.databaseBuilder<FixtureDatabaseV1>(context, name).configureDatabaseDriver().build()
            try { original.entries().insert(EntryV1(1, "preserved")) }
            finally { original.close() }

            val migrated = Room.databaseBuilder<FixtureDatabaseV2>(context, name)
                .addMigrations(fixtureMigration).configureDatabaseDriver().build()
            try { assertEquals(listOf(EntryV2(1, "preserved", "untitled")), migrated.entries().entries()) }
            finally { migrated.close() }
        } finally { context.deleteDatabase(name) }
    }

    @Test
    fun missingMigrationFailsWithoutDeletingOriginalRows(): Unit = runBlocking {
        val name = "core-database-test-${UUID.randomUUID()}.db"
        try {
            val original = Room.databaseBuilder<FixtureDatabaseV1>(context, name).configureDatabaseDriver().build()
            try { original.entries().insert(EntryV1(1, "must survive")) }
            finally { original.close() }

            val unsupported = Room.databaseBuilder<FixtureDatabaseV2>(context, name).configureDatabaseDriver().build()
            try { assertFailsWith<IllegalStateException> { unsupported.entries().entries() } }
            finally { unsupported.close() }

            val reopened = Room.databaseBuilder<FixtureDatabaseV1>(context, name).configureDatabaseDriver().build()
            try { assertEquals(listOf(EntryV1(1, "must survive")), reopened.entries().entries()) }
            finally { reopened.close() }
        } finally { context.deleteDatabase(name) }
    }
}
