package com.xiongxianfei.honorkingsrecorder.ui.screens.review

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.xiongxianfei.honorkingsrecorder.util.ScreenshotParser
import com.xiongxianfei.honorkingsrecorder.util.coach.CoachRuleEngine
import com.xiongxianfei.honorkingsrecorder.util.coach.CoachTip
import com.xiongxianfei.honorkingsrecorder.util.coach.FrameData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class VideoReviewUiState(
    val videoUri: Uri? = null,
    val isProcessing: Boolean = false,
    val processingStep: String = "",
    val frames: List<FrameData> = emptyList(),
    val tips: List<CoachTip> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class VideoReviewViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(VideoReviewUiState())
    val state: StateFlow<VideoReviewUiState> = _state.asStateFlow()

    /** Checkpoints: (label, timestamp in ms, timestamp in µs for retriever) */
    val checkpoints = listOf(
        Triple("4:30",  270_000L,  270_000_000L),
        Triple("7:30",  450_000L,  450_000_000L),
        Triple("10:30", 630_000L,  630_000_000L),
    )

    fun setVideoUri(uri: Uri) {
        _state.update { it.copy(videoUri = uri, tips = emptyList(), frames = emptyList(), error = null) }
    }

    fun processVideo(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, error = null, tips = emptyList()) }
            try {
                val frames = mutableListOf<FrameData>()

                for ((label, timestampMs, timestampUs) in checkpoints) {
                    _state.update { it.copy(processingStep = "正在识别 $label…") }

                    val bitmap = withContext(Dispatchers.IO) {
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(context, uri)
                            retriever.getFrameAtTime(
                                timestampUs,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )
                        } finally {
                            retriever.release()
                        }
                    }

                    if (bitmap == null) {
                        frames += FrameData(timestampMs, null, null, null, null)
                        continue
                    }

                    val image = InputImage.fromBitmap(bitmap, 0)
                    val recognizer = TextRecognition.getClient(
                        ChineseTextRecognizerOptions.Builder().build()
                    )
                    val visionText = suspendCancellableCoroutine { cont ->
                        recognizer.process(image)
                            .addOnSuccessListener { cont.resume(it) }
                            .addOnFailureListener { cont.resumeWithException(it) }
                    }

                    val parsed = ScreenshotParser.parse(visionText.text)
                    frames += FrameData(
                        timestampMs = timestampMs,
                        economy  = parsed.economy,
                        deaths   = parsed.deaths,
                        kills    = parsed.kills,
                        assists  = parsed.assists,
                    )
                }

                val tips = CoachRuleEngine.analyze(frames)
                _state.update {
                    it.copy(
                        isProcessing  = false,
                        processingStep = "",
                        frames = frames,
                        tips   = tips
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isProcessing = false, processingStep = "", error = "分析失败：${e.message}")
                }
            }
        }
    }
}
