package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeJournalDao {
    @Query("SELECT * FROM trade_journal ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeJournalEntity>>

    @Query("SELECT * FROM trade_journal WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getTradesBySymbol(symbol: String): Flow<List<TradeJournalEntity>>

    @Query("SELECT * FROM trade_journal WHERE outcome = :outcome ORDER BY timestamp DESC")
    fun getTradesByOutcome(outcome: String): Flow<List<TradeJournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeJournalEntity): Long

    @Update
    suspend fun updateTrade(trade: TradeJournalEntity)

    @Query("DELETE FROM trade_journal WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("DELETE FROM trade_journal")
    suspend fun clearAll()
}
