package com.example.floodwatch.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {

    @Query("SELECT * FROM analysis_records ORDER BY timestampMs DESC")
    fun getAll(): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analysis_records WHERE severity = :severity ORDER BY timestampMs DESC")
    fun getBySeverity(severity: String): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analysis_records WHERE title LIKE '%' || :query || '%' ORDER BY timestampMs DESC")
    fun search(query: String): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analysis_records WHERE title LIKE '%' || :query || '%' AND severity = :severity ORDER BY timestampMs DESC")
    fun searchWithSeverity(query: String, severity: String): Flow<List<AnalysisRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AnalysisRecord): Long

    @Delete
    suspend fun delete(record: AnalysisRecord)

    @Query("DELETE FROM analysis_records")
    suspend fun deleteAll()
}
