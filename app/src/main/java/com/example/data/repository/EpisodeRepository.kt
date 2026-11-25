package com.example.data.repository

import com.example.data.local.EpisodeDao
import com.example.data.local.EpisodeEntity
import com.example.domain.model.AuditReport
import com.example.domain.model.EndingType
import com.example.domain.model.EpisodeResult
import com.example.domain.model.GeneratedStory
import com.example.domain.model.HorrorMechanism
import com.example.domain.model.OpeningHookType
import com.example.domain.model.StoryCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class EpisodeRepository(private val dao: EpisodeDao) {

    val allEpisodes: Flow<List<EpisodeResult>> = dao.observeAllEpisodes().map { entities ->
        entities.mapNotNull { it.toDomainOrNull() }
    }

    suspend fun saveEpisode(episode: EpisodeResult): Long {
        return dao.insertEpisode(episode.toEntity())
    }

    suspend fun deleteEpisode(id: Long) {
        dao.deleteEpisodeById(id)
    }

    private fun EpisodeResult.toEntity(): EpisodeEntity {
        val arr = JSONArray()
        stories.forEach { story ->
            val obj = JSONObject().apply {
                put("storyId", story.storyId)
                put("chapterTitle", story.chapterTitle)
                put("category", story.category.name)
                put("mechanism", story.mechanism.name)
                put("openingHookType", story.openingHookType.name)
                put("endingType", story.endingType.name)
                put("hiddenSpine", story.hiddenSpine)
                put("ttsReadyText", story.ttsReadyText)
                put("targetChars", story.targetChars)
                put("audit", JSONObject().apply {
                    put("zeroDigitsPassed", story.auditReport.zeroDigitsPassed)
                    put("brandLockPassed", story.auditReport.brandLockPassed)
                    put("povLockPassed", story.auditReport.povLockPassed)
                    put("sfxValidPassed", story.auditReport.sfxValidPassed)
                    put("engineUniquePassed", story.auditReport.engineUniquePassed)
                    put("spokenCharCount", story.auditReport.spokenCharCount)
                    put("targetCharCount", story.auditReport.targetCharCount)
                    put("detectedCliches", JSONArray(story.auditReport.detectedCliches))
                    put("repairsApplied", JSONArray(story.auditReport.repairsApplied))
                })
            }
            arr.put(obj)
        }
        return EpisodeEntity(
            id = id,
            title = title,
            categoryName = category.name,
            createdAtMillis = createdAtMillis,
            storiesJson = arr.toString(),
            ttsReadyExport = ttsReadyExport,
            fullMasterExport = fullMasterExport,
            totalSpokenChars = totalSpokenChars,
            totalTargetChars = totalTargetChars,
            modelName = modelName
        )
    }

    private fun EpisodeEntity.toDomainOrNull(): EpisodeResult? {
        return try {
            val category = runCatching { StoryCategory.valueOf(categoryName) }
                .getOrDefault(StoryCategory.GENERAL_FICTION)
            val arr = JSONArray(storiesJson)
            val stories = buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val auditObj = obj.optJSONObject("audit") ?: JSONObject()
                    val clichesArr = auditObj.optJSONArray("detectedCliches") ?: JSONArray()
                    val repairsArr = auditObj.optJSONArray("repairsApplied") ?: JSONArray()
                    add(
                        GeneratedStory(
                            storyId = obj.getString("storyId"),
                            chapterTitle = obj.getString("chapterTitle"),
                            category = runCatching { StoryCategory.valueOf(obj.getString("category")) }
                                .getOrDefault(category),
                            mechanism = runCatching { HorrorMechanism.valueOf(obj.getString("mechanism")) }
                                .getOrDefault(HorrorMechanism.PSYCHOLOGICAL_IDENTITY_DOUBT),
                            openingHookType = runCatching { OpeningHookType.valueOf(obj.getString("openingHookType")) }
                                .getOrDefault(OpeningHookType.CONFESSIONAL_ADMISSION),
                            endingType = runCatching { EndingType.valueOf(obj.getString("endingType")) }
                                .getOrDefault(EndingType.AMBIGUOUS_RESIDUE),
                            hiddenSpine = obj.optString("hiddenSpine"),
                            ttsReadyText = obj.getString("ttsReadyText"),
                            targetChars = obj.optInt("targetChars", 1000),
                            auditReport = AuditReport(
                                zeroDigitsPassed = auditObj.optBoolean("zeroDigitsPassed", true),
                                brandLockPassed = auditObj.optBoolean("brandLockPassed", true),
                                povLockPassed = auditObj.optBoolean("povLockPassed", true),
                                sfxValidPassed = auditObj.optBoolean("sfxValidPassed", true),
                                engineUniquePassed = auditObj.optBoolean("engineUniquePassed", true),
                                spokenCharCount = auditObj.optInt("spokenCharCount", 0),
                                targetCharCount = auditObj.optInt("targetCharCount", 1000),
                                detectedCliches = List(clichesArr.length()) { idx -> clichesArr.getString(idx) },
                                repairsApplied = List(repairsArr.length()) { idx -> repairsArr.getString(idx) }
                            )
                        )
                    )
                }
            }
            EpisodeResult(
                id = id,
                title = title,
                category = category,
                createdAtMillis = createdAtMillis,
                stories = stories,
                ttsReadyExport = ttsReadyExport,
                fullMasterExport = fullMasterExport,
                totalSpokenChars = totalSpokenChars,
                totalTargetChars = totalTargetChars,
                modelName = modelName
            )
        } catch (_: Exception) {
            null
        }
    }
}
