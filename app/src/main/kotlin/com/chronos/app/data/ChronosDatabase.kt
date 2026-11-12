package com.chronos.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Authoritative local store. Schedules are the durable source of truth that the
// reconciler re-arms after reboot (PRD reliability requirement).
@Database(
 entities = [
 ScheduleEntity::class,
 WorkflowEntity::class,
 ExecutionEntity::class,
 AuditEntity::class,
 ],
 version = 1,
 exportSchema = false,
)
abstract class ChronosDatabase : RoomDatabase() {
 abstract fun scheduleDao(): ScheduleDao
 abstract fun workflowDao(): WorkflowDao
 abstract fun executionDao(): ExecutionDao
 abstract fun auditDao(): AuditDao

 companion object {
 @Volatile
 private var instance: ChronosDatabase? = null

 fun get(context: Context): ChronosDatabase =
 instance ?: synchronized(this) {
 instance ?: Room.databaseBuilder(
 context.applicationContext,
 ChronosDatabase::class.java,
 "chronos.db",
 ).fallbackToDestructiveMigration().build().also { instance = it }
 }
 }
}
