package com.kreedzt.robin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import android.os.SystemClock
import android.util.Log
import com.kreedzt.robin.R

@Composable
fun MapImagePreview(
    mapImageUrl: String,
    mapName: String,
    mapId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    var startTs by remember { mutableLongStateOf(0L) }

    // 按弹窗最大尺寸下采样，减少解码时间与内存
    val (targetWidthPx, targetHeightPx) = remember(configuration, density) {
        val widthPx = with(density) { (configuration.screenWidthDp * 0.95f).dp.roundToPx() }
        val heightPx = with(density) { (configuration.screenHeightDp * 0.85f).dp.roundToPx() }
        widthPx to heightPx
    }

    val imageRequest = remember(mapImageUrl) {
        ImageRequest.Builder(context)
            .data(mapImageUrl)
            .setHeader("Accept", "image/avif,image/webp,*/*")
            .size(Size(targetWidthPx, targetHeightPx))
            .precision(Precision.INEXACT)
            .crossfade(true)
            .listener(
                onStart = {
                    startTs = SystemClock.elapsedRealtime()
                    Log.d("MapImagePreview", "start url=$mapImageUrl name=$mapName")
                },
                onSuccess = { _, result ->
                    val cost = SystemClock.elapsedRealtime() - startTs
                    Log.d(
                        "MapImagePreview",
                        "success url=$mapImageUrl name=$mapName cost=${cost}ms size=${result.drawable.intrinsicWidth}x${result.drawable.intrinsicHeight}"
                    )
                },
                onError = { _, error ->
                    val cost = if (startTs > 0) SystemClock.elapsedRealtime() - startTs else -1
                    Log.w("MapImagePreview", "error url=$mapImageUrl name=$mapName cost=${cost}ms ex=${error.throwable.message}")
                }
            )
            .build()
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close_dialog),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 图片容器
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = false) { }, // 防止点击事件穿透
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    // 地图名称标题
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = mapName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = mapId,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // 图片内容
                    Box(modifier = Modifier.weight(1f)) {
                        SubcomposeAsyncImage(
                            model = imageRequest,
                            contentDescription = "地图预览: $mapName",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(48.dp),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = stringResource(R.string.map_image_loading_dialog),
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🗺️",
                                            style = MaterialTheme.typography.headlineLarge
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.map_image_error_dialog),
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = mapName,
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}