package com.chronos.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
 @Query("SELECT * FROM schedules ORDER BY name")
 fun observeAll(): Flow<List<ScheduleEntity>>

 @Query("SELECT * FROM schedules WHERE enabled = 1")
 suspend fun enabled(): List<ScheduleEntity>

 @Query("SELECT * FROM schedules WHERE id = :id")
 suspend fun byId(id: String): ScheduleEntity?

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun upsert(schedule: ScheduleEntity)

 @Update
 suspend fun update(schedule: ScheduleEntity)

 @Query("DELETE FROM schedules WHERE id = :id")
 suspend fun delete(id: String)
}

@Dao
interface WorkflowDao {
 @Query("SELECT * FROM workflows ORDER BY name")
 fun observeAll(): Flow<List<WorkflowEntity>>

 @Query("SELECT * FROM workflows WHERE id = :id")
 suspend fun byId(id: String): WorkflowEntity?

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun upsert(workflow: WorkflowEntity)

 @Query("DELETE FROM workflows WHERE id = :id")
 suspend fun delete(id: String)
}

@Dao
interface ExecutionDao {
 @Query("SELECT * FROM executions ORDER BY scheduledAt DESC LIMIT :limit")
 fun observeRecent(limit: Int = 100): Flow<List<ExecutionEntity>>

 @Insert
 suspend fun insert(execution: ExecutionEntity): Long

 @Query("SELECT * FROM executions ORDER BY scheduledAt DESC LIMIT 1")
 suspend fun lastExecution(): ExecutionEntity?
}

@Dao
interface AuditDao {
 @Query("SELECT * FROM audit ORDER BY timestamp DESC LIMIT :limit")
 fun observeRecent(limit: Int = 200): Flow<List<AuditEntity>>

 @Insert
 suspend fun insert(entry: AuditEntity)
}
