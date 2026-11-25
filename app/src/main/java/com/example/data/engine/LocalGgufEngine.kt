package com.example.data.engine

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.example.domain.model.EngineStatus
import com.example.domain.model.InferenceSettings
import com.llamatik.library.platform.GenStream
import com.llamatik.library.platform.LlamaBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class PreparedModelFile(
    val path: String,
    val displayName: String,
    val sizeBytes: Long
)

class LocalGgufEngine(private val context: Context) {

    private val _engineStatus = MutableStateFlow<EngineStatus>(EngineStatus.Unloaded)
    val engineStatus: StateFlow<EngineStatus> = _engineStatus.asStateFlow()

    private val _stagingProgress = MutableStateFlow<Float?>(null)
    val stagingProgress: StateFlow<Float?> = _stagingProgress.asStateFlow()

    private var loadedPath: String? = null

    suspend fun prepareModelFromUri(uri: Uri): Result<PreparedModelFile> = withContext(Dispatchers.IO) {
        runCatching {
            val (displayName, sizeBytes) = queryFileMeta(uri)
            require(displayName.endsWith(".gguf", ignoreCase = true) || sizeBytes > 0L) {
                "Please select a valid .gguf model file."
            }

            // First check if a direct POSIX path is readable by llama.cpp mmap
            val directPath = tryResolveDirectPath(uri)
            if (directPath != null) {
                val directFile = File(directPath)
                if (directFile.exists() && directFile.canRead() && directFile.length() > 0L) {
                    return@runCatching PreparedModelFile(
                        path = directFile.absolutePath,
                        displayName = directFile.name,
                        sizeBytes = directFile.length()
                    )
                }
            }

            // Otherwise stage into app-private filesDir/models/ via SAF stream (zero broad storage permissions)
            val modelsDir = File(context.filesDir, "models").apply { if (!exists()) mkdirs() }
            val safeName = displayName.replace(Regex("""[^a-zA-Z0-9._-]"""), "_")
                .let { if (it.endsWith(".gguf", ignoreCase = true)) it else "$it.gguf" }
            val targetFile = File(modelsDir, safeName)

            // Clean up any other stale staged models to protect internal storage
            modelsDir.listFiles()?.forEach { existing ->
                if (existing.absolutePath != targetFile.absolutePath) {
                    existing.delete()
                }
            }

            if (!targetFile.exists() || targetFile.length() != sizeBytes || sizeBytes <= 0L) {
                _stagingProgress.value = 0f
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(1024 * 1024)
                        var copied = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            copied += read
                            if (sizeBytes > 0L) {
                                _stagingProgress.value = (copied.toFloat() / sizeBytes.toFloat()).coerceIn(0f, 1f)
                            }
                        }
                        output.flush()
                    }
                } ?: error("Cannot open selected .gguf file stream.")
                _stagingProgress.value = null
            }

            PreparedModelFile(
                path = targetFile.absolutePath,
                displayName = displayName,
                sizeBytes = targetFile.length()
            )
        }.also {
            _stagingProgress.value = null
        }
    }

    suspend fun loadModel(
        path: String,
        displayName: String,
        settings: InferenceSettings
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists() || !file.canRead() || file.length() <= 0L) {
            val err = "Model file not found or unreadable at $path"
            _engineStatus.value = EngineStatus.Error(err)
            return@withContext Result.failure(IllegalStateException(err))
        }

        _engineStatus.value = EngineStatus.Loading(displayName)
        return@withContext runCatching {
            if (loadedPath != null) {
                runCatching { LlamaBridge.shutdown() }
                loadedPath = null
            }

            val defaultMaxTokens = computeMaxTokensBudget(settings.contextLength)
            LlamaBridge.updateGenerateParams(
                temperature = settings.temperature,
                maxTokens = defaultMaxTokens,
                topP = settings.topP,
                topK = settings.topK,
                repeatPenalty = settings.repeatPenalty
            )

            val initialized = LlamaBridge.initGenerateModel(file.absolutePath)
            check(initialized) {
                "llama.cpp failed to initialize $displayName. Ensure enough free RAM is available."
            }
            loadedPath = file.absolutePath
            _engineStatus.value = EngineStatus.Ready(
                modelName = displayName,
                modelPath = file.absolutePath,
                sizeBytes = file.length()
            )
        }.onFailure { throwable ->
            loadedPath = null
            _engineStatus.value = EngineStatus.Error(
                throwable.message ?: "Failed to load .gguf model into native memory."
            )
        }
    }

    suspend fun unloadModel() = withContext(Dispatchers.IO) {
        runCatching { LlamaBridge.nativeCancelGenerate() }
        runCatching { LlamaBridge.shutdown() }
        loadedPath = null
        _engineStatus.value = EngineStatus.Unloaded
    }

    fun streamWithContext(
        systemPrompt: String,
        contextBlock: String,
        userPrompt: String,
        settings: InferenceSettings,
        maxTokensOverride: Int? = null
    ): Flow<String> = callbackFlow {
        val currentStatus = _engineStatus.value
        if (currentStatus !is EngineStatus.Ready) {
            close(IllegalStateException("No .gguf model loaded. Load a local model in Engine & Vault first."))
            return@callbackFlow
        }

        val maxTokens = (maxTokensOverride ?: computeMaxTokensBudget(settings.contextLength))
            .coerceIn(256, settings.contextLength - 512)

        val isChatMlModel = currentStatus.modelName.lowercase().let { name ->
            name.contains("qwen") || name.contains("hermes") || name.contains("dolphin") || name.contains("chatml")
        }

        val job = launch(Dispatchers.IO) {
            try {
                LlamaBridge.updateGenerateParams(
                    temperature = settings.temperature,
                    maxTokens = maxTokens,
                    topP = settings.topP,
                    topK = settings.topK,
                    repeatPenalty = settings.repeatPenalty
                )

                val callback = object : GenStream {
                    override fun onDelta(text: String) {
                        val cleaned = text
                            .replace("<|im_end|>", "")
                            .replace("<|endoftext|>", "")
                            .replace("<|eot_id|>", "")
                        if (cleaned.isNotEmpty()) {
                            trySend(cleaned)
                        }
                    }

                    override fun onComplete() {
                        close()
                    }

                    override fun onError(message: String) {
                        close(IllegalStateException(message.ifBlank { "Native inference error." }))
                    }
                }

                if (isChatMlModel) {
                    val chatMlPrompt = buildString {
                        append("<|im_start|>system\n")
                        append(systemPrompt)
                        if (contextBlock.isNotBlank()) {
                            append("\n\n[EXECUTION CONTEXT]\n")
                            append(contextBlock)
                        }
                        append("<|im_end|>\n<|im_start|>user\n")
                        append(userPrompt)
                        append("<|im_end|>\n<|im_start|>assistant\n<think>\n</think>\n\n")
                    }
                    LlamaBridge.generateStream(chatMlPrompt, callback)
                } else {
                    LlamaBridge.generateStreamWithContext(
                        systemPrompt = systemPrompt,
                        contextBlock = contextBlock,
                        userPrompt = userPrompt,
                        callback = callback
                    )
                }
            } catch (t: Throwable) {
                close(t)
            }
        }

        awaitClose {
            runCatching { LlamaBridge.nativeCancelGenerate() }
            job.cancel()
        }
    }

    fun cancelGeneration() {
        runCatching { LlamaBridge.nativeCancelGenerate() }
    }

    private fun computeMaxTokensBudget(contextLength: Int): Int {
        return when {
            contextLength >= 8192 -> 3200
            contextLength >= 6144 -> 2400
            else -> 1600
        }
    }

    private fun queryFileMeta(uri: Uri): Pair<String, Long> {
        var name = "model.gguf"
        var size = -1L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
            }
        }
        return name to size
    }

    private fun tryResolveDirectPath(uri: Uri): String? {
        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        if (DocumentsContract.isDocumentUri(context, uri) &&
            uri.authority == "com.android.externalstorage.documents"
        ) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            if (split.size == 2 && split[0].equals("primary", ignoreCase = true)) {
                return "${Environment.getExternalStorageDirectory().absolutePath}/${split[1]}"
            }
        }
        return null
    }
}
