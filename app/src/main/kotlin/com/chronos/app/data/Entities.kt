package com.chronos.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Persisted schedule. The runtime fires it according to kind + recurrence JSON.
@Entity(tableName = "schedules")
data class ScheduleEntity(
 @PrimaryKey val id: String,
 val name: String,
 val kind: String,
 val recurrenceJson: String?,
 val fixedDateTime: String?,
 val enabled: Boolean,
 val nextRun: String?,
 val createdAt: Long = System.currentTimeMillis(),
)

// Persisted workflow definition (validated JSON produced by the compiler).
@Entity(tableName = "workflows")
data class WorkflowEntity(
 @PrimaryKey val id: String,
 val name: String,
 val schemaVersion: Int,
 val workflowJson: String,
 val enabled: Boolean = true,
 val createdAt: Long = System.currentTimeMillis(),
)

// One row per execution attempt (diagnostics + audit).
@Entity(tableName = "executions")
data class ExecutionEntity(
 @PrimaryKey(autoGenerate = true) val id: Long = 0,
 val scheduleId: String?,
 val workflowId: String?,
 val state: String,
 val scheduledAt: Long,
 val startedAt: Long?,
 val finishedAt: Long?,
 val errorCode: String?,
 val errorMessage: String?,
 val retryCount: Int = 0,
)

// Security/audit trail for important changes.
@Entity(tableName = "audit")
data class AuditEntity(
 @PrimaryKey(autoGenerate = true) val id: Long = 0,
 val timestamp: Long = System.currentTimeMillis(),
 val category: String,
 val message: String,
)
