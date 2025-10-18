package com.example.owoo.ui.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.owoo.ui.home.HomeState
import com.example.owoo.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisenseDetailScreen(
    homeState: HomeState,
    viewModel: HomeViewModel
) {
    val hisenseData = homeState.rowDetails?.hisenseData ?: return // Safety check
    val images = remember { hisenseData.images?.filterValues { !it.isNullOrBlank() }?.toList() ?: emptyList() }
    var currentPage by remember { mutableStateOf(0) }
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 56.dp, // Height for the "peek" state
        sheetContent = {
            EvaluationSheetContent(
                currentPage = currentPage,
                evaluationForm = homeState.evaluationForm,
                onFormChange = { col, value -> viewModel.updateEvaluation(col, value) },
                rejectionMessages = homeState.rejectionMessages
            )
            // Add extra space for the sheet to go behind navigation bars
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    ) { paddingValues ->
        if (images.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada gambar untuk ditampilkan.", modifier = Modifier.padding(16.dp))
            }
            return@BottomSheetScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dynamic Info Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val currentImageTitle = images.getOrNull(currentPage)?.first ?: "Detail"
                    Text(
                        text = currentImageTitle,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("NPSN: ${hisenseData.schoolInfo?.npsn ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Nama: ${hisenseData.schoolInfo?.nama ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Image Viewer with Navigation
            ImageViewerWithControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                imageUrl = images[currentPage].second ?: "",
                onPrevious = {
                    currentPage = if (currentPage == 0) images.lastIndex else currentPage - 1
                },
                onNext = {
                    currentPage = if (currentPage == images.lastIndex) 0 else currentPage + 1
                },
                showNavigation = images.size > 1
            )

            // Pager Indicator
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { iteration ->
                    val color = if (currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageViewerWithControls(
    modifier: Modifier = Modifier,
    imageUrl: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    showNavigation: Boolean
) {
    Box(modifier = modifier) {
        ZoomableAsyncImage(
            imageUrl = imageUrl,
            modifier = Modifier.fillMaxSize()
        )

        if (showNavigation) {
            // Previous Area
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.2f) // 20% of the width
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPrevious
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }

            // Next Area
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.2f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNext
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Next",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }
    }
}


@Composable
fun ZoomableAsyncImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    var composableSize by remember { mutableStateOf(IntSize.Zero) }

    // Reset transform state when image url changes
    LaunchedEffect(imageUrl) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        rotation = 0f
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .background(Color.Black)
            .onSizeChanged { composableSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val oldScale = scale
                    scale *= zoom
                    scale = scale.coerceIn(1f, 5f)

                    if (scale > 1f) {
                        val maxOffsetX = (composableSize.width * (scale - 1)) / 2f
                        val maxOffsetY = (composableSize.height * (scale - 1)) / 2f
                        
                        offsetX = (offsetX + pan.x * oldScale).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + pan.y * oldScale).coerceIn(-maxOffsetY, maxOffsetY)
                    } else {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Gambar Hisense",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY,
                    rotationZ = rotation
                )
        )

        // Control Buttons for Zoom/Rotate
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            IconButton(onClick = {
                scale = 1f
                offsetX = 0f
                offsetY = 0f
                rotation = 0f
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset View", tint = Color.White)
            }
            IconButton(onClick = { rotation += 90f }) {
                Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = Color.White)
            }
        }
    }
}