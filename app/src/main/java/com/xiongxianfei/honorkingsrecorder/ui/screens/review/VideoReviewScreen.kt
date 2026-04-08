package com.xiongxianfei.honorkingsrecorder.ui.screens.review

import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xiongxianfei.honorkingsrecorder.util.coach.CoachTip

@Composable
fun VideoReviewScreen(
    viewModel: VideoReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.setVideoUri(uri)
            videoView?.setVideoURI(uri)
            videoView?.start()
            viewModel.processVideo(uri)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Video player ────────────────────────────────────────────────────
        if (state.videoUri != null) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val mc = MediaController(ctx)
                        mc.setAnchorView(this)
                        setMediaController(mc)
                        state.videoUri?.let { setVideoURI(it) }
                        videoView = this
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            // ── Checkpoint seek chips ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.checkpoints.forEach { (label, timestampMs, _) ->
                    FilterChip(
                        selected = false,
                        onClick = { videoView?.seekTo(timestampMs.toInt()) },
                        label = { Text(label) }
                    )
                }
            }
        }

        // ── Tips list ───────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when {
                state.isProcessing -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(state.processingStep, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                state.error != null -> item {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                state.tips.isNotEmpty() -> {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("教练建议", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider()
                    }

                    // Group tips by timestamp, show a header for each checkpoint
                    val grouped = state.tips.groupBy { it.timestampMs }
                    viewModel.checkpoints.forEach { (label, timestampMs, _) ->
                        val tipsForTs = grouped[timestampMs] ?: return@forEach
                        item {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "⏱ $label",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(tipsForTs) { tip ->
                            CoachTipRow(tip)
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                state.videoUri == null -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "选择一段比赛视频开始复盘",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Pick-video button ────────────────────────────────────────────────
        if (!state.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                ElevatedButton(onClick = { videoPicker.launch("video/*") }) {
                    Text(if (state.videoUri == null) "选择视频" else "重新选择")
                }
            }
        }
    }
}

@Composable
private fun CoachTipRow(tip: CoachTip) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (tip.isPositive) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (tip.isPositive) Color(0xFF4CAF50) else Color(0xFFFF9800),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = tip.message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
