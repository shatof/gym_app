package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.model.Measurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: Measurement): Long

    @Update
    suspend fun update(measurement: Measurement)

    @Delete
    suspend fun delete(measurement: Measurement)

    @Query("SELECT * FROM measurements ORDER BY date DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements ORDER BY date DESC LIMIT 1")
    fun getLatestMeasurement(): Flow<Measurement?>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): Measurement?

    @Query("SELECT * FROM measurements WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getMeasurementsInRange(startDate: Long, endDate: Long): Flow<List<Measurement>>

    @Query("DELETE FROM measurements")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM measurements")
    suspend fun getCount(): Int
}

