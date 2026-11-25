package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val categoryName: String,
    val createdAtMillis: Long,
    val storiesJson: String,
    val ttsReadyExport: String,
    val fullMasterExport: String,
    val totalSpokenChars: Int,
    val totalTargetChars: Int,
    val modelName: String
)
