package com.chronos.app.data

import kotlinx.coroutines.flow.Flow

// Thin repository over the Room DAOs. Keeps the rest of the app independent of
// Room and gives a single place to add caching/validation later.
class ScheduleRepository(
 private val scheduleDao: ScheduleDao,
 private val executionDao: ExecutionDao,
 private val auditDao: AuditDao,
) {
 fun observeSchedules(): Flow<List<ScheduleEntity>> = scheduleDao.observeAll()

 suspend fun enabledSchedules(): List<ScheduleEntity> = scheduleDao.enabled()

 suspend fun get(id: String): ScheduleEntity? = scheduleDao.byId(id)

 suspend fun upsert(entity: ScheduleEntity) {
 scheduleDao.upsert(entity)
 auditDao.insert(AuditEntity(category = "schedule", message = "upsert " + entity.id))
 }

 suspend fun delete(id: String) {
 scheduleDao.delete(id)
 auditDao.insert(AuditEntity(category = "schedule", message = "delete " + id))
 }

 fun observeExecutions(): Flow<List<ExecutionEntity>> = executionDao.observeRecent()
}
