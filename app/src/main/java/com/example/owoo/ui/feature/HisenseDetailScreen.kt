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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.owoo.ui.home.HomeState
import com.example.owoo.data.datadik.DatadikData
import com.example.owoo.data.datadik.Ptk
import com.example.owoo.ui.home.HomeViewModel
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import androidx.compose.foundation.gestures.detectTapGestures



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisenseDetailScreen(
    homeState: HomeState,
    viewModel: HomeViewModel
) {
    val hisenseData = homeState.rowDetails?.hisenseData ?: return
    val datadikData = homeState.rowDetails?.datadikData
    val images = remember { hisenseData.images?.filterValues { !it.isNullOrBlank() }?.toList() ?: emptyList() }
    var currentPage by remember { mutableStateOf(0) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    LaunchedEffect(hisenseData) {
        images.forEach { (_, imageUrl) ->
            if (imageUrl.isNotBlank()) {
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }

    if (scaffoldState.bottomSheetState.isVisible) {
        BackHandler(onBack = {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        })
    }

    var ptkQuery by remember { mutableStateOf("") }
    val ptkList = remember(datadikData) { datadikData?.ptk ?: emptyList() }
    val filteredPtk = remember(ptkQuery, ptkList) {
        if (ptkQuery.length < 1) {
            emptyList()
        } else {
            ptkList.filter { it.nama?.contains(ptkQuery, ignoreCase = true) == true }
        }
    }
    var selectedPtk by remember { mutableStateOf<Ptk?>(null) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 30.dp,
        sheetContent = {
            EvaluationSheetContent(
                currentPage = currentPage,
                evaluationForm = homeState.evaluationForm,
                onFormChange = { col, value -> viewModel.updateEvaluation(col, value) },
                rejectionMessages = homeState.rejectionMessages,
                rejectionReasonString = homeState.rejectionReasonString,
                onReasonChange = { viewModel.updateRejectionReason(it) }
            )
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
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val currentImageTitle = images.getOrNull(currentPage)?.first ?: "Detail"

                    val dynamicInfo = getDynamicInfo(currentImageTitle, hisenseData, datadikData)
                    val isBappScreen = currentImageTitle == "Foto BAPP Hal 1" || currentImageTitle == "Foto BAPP Hal 2"

                    if (dynamicInfo.isNotEmpty()) {
                        dynamicInfo.forEach { (label, value) ->
                            if (label == "NPSN - Nama") {
                                Text("$value", style = MaterialTheme.typography.bodySmall)
                                HorizontalDivider(thickness = 1.dp, color = Color.White)
                                Spacer(modifier = Modifier.height(1.dp))
                            } else {
                                Text("$label: $value", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        val npsn = hisenseData.schoolInfo?.npsn ?: "N/A"
                        val nama = hisenseData.schoolInfo?.nama ?: "N/A"
                        Text("NPSN - Nama: $npsn - $nama", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (isBappScreen) {
                        OutlinedTextField(
                            value = ptkQuery,
                            onValueChange = { ptkQuery = it; selectedPtk = null },
                            label = { Text("Cari Nama PTK (min. 1 huruf)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (ptkQuery.length >= 1 && filteredPtk.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            filteredPtk.take(5).forEach { ptk ->
                                Column(modifier = Modifier
                                    .clickable {
                                        selectedPtk = ptk
                                        ptkQuery = ptk.nama ?: ""
                                    }
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp)) {
                                    Text(ptk.nama ?: "", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        selectedPtk?.let { ptk ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nama PTK: ${ptk.nama ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Jabatan: ${ptk.jabatanPtk ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                            Text("NIP: ${ptk.nip ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
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

private fun getDynamicInfo(
    currentImageTitle: String,
    hisenseData: com.example.owoo.data.hisense.HisenseData,
    datadikData: DatadikData?
): List<Pair<String, String>> {
    val info = mutableListOf<Pair<String, String>>()
    val schoolInfo = hisenseData.schoolInfo ?: return emptyList()

    fun getHisenseFullAddress(schoolInfo: com.example.owoo.data.hisense.HisenseSchoolInfo): String {
        val parts = listOfNotNull(schoolInfo.alamat, schoolInfo.kecamatan, schoolInfo.kabupaten)
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "N/A"
    }

    fun getDatadikFullAddress(datadikData: DatadikData?): String {
        if (datadikData == null) return "N/A"
        val parts = listOfNotNull(datadikData.address, datadikData.kecamatan, datadikData.kabupaten)
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "N/A"
    }

    // Helper to add NPSN - Nama
    fun addNpsnNamaInfo() {
        val npsn = schoolInfo.npsn ?: "N/A"
        val nama = schoolInfo.nama ?: "N/A"
        info.add("NPSN - Nama" to "$npsn - $nama")
    }

    when (currentImageTitle) {
        "Foto Plang Sekolah" -> {
            addNpsnNamaInfo()
            info.add("Alamat" to getHisenseFullAddress(schoolInfo))
            info.add("Alamat Datadik" to getDatadikFullAddress(datadikData))
        }
        "Foto Box & PIC", "Foto Kelengkapan Unit", "Foto Proses Instalasi", "Foto Training" -> {
            info.add("Alamat" to getHisenseFullAddress(schoolInfo))
            info.add("Alamat Datadik" to getDatadikFullAddress(datadikData))
        }
        "Foto Serial Number" -> {
            addNpsnNamaInfo()
            info.add("Serial Number" to (schoolInfo.serialNumber ?: "N/A"))
            info.add("Alamat" to getHisenseFullAddress(schoolInfo))
            info.add("Alamat Datadik" to getDatadikFullAddress(datadikData))
        }
        "Foto BAPP Hal 1" -> {
            addNpsnNamaInfo()
            info.add("Serial Number" to (schoolInfo.serialNumber ?: "N/A"))
            info.add("PIC" to (schoolInfo.pic ?: "N/A"))
        }
        "Foto BAPP Hal 2" -> {
            addNpsnNamaInfo()
            info.add("PIC" to (schoolInfo.pic ?: "N/A"))
        }
    }
    return info
}

@Composable
fun ImageViewerWithControls(
    modifier: Modifier = Modifier,
    imageUrl: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    showNavigation: Boolean
) {
    var isFullscreen by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    var composableSize by remember { mutableStateOf(IntSize.Zero) }
    var reloadTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(imageUrl) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        rotation = 0f
    }

    fun resetTransform() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Column(modifier = modifier) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
                .onSizeChanged { composableSize = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale < 2f) {
                                scale = 2.5f
                            } else {
                                resetTransform()
                            }
                        }
                    )
                }
                .pointerInput(imageUrl) {
                    detectTransformGestures { _, pan, zoom, _ ->

                        val prevScale = scale
                        scale = (scale * zoom).coerceIn(1f, 4f)

                        if (scale > 1f) {
                            val maxX = (composableSize.width * (scale - 1)) / 2f
                            val maxY = (composableSize.height * (scale - 1)) / 2f

                            offsetX = (offsetX + pan.x * prevScale).coerceIn(-maxX, maxX)
                            offsetY = (offsetY + pan.y * prevScale).coerceIn(-maxY, maxY)
                        } else {
                            resetTransform()
                        }
                    }
                }
        ) {
            AsyncImage(
                model = "$imageUrl?reload=$reloadTrigger",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY,
                        rotationZ = rotation
                    ),
                contentScale = ContentScale.Fit
            )

            if (showNavigation) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .fillMaxWidth(0.2f)
                        .clickable { onPrevious() }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .fillMaxWidth(0.2f)
                        .clickable { onNext() }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { resetTransform() }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
            IconButton(onClick = { rotation += 90f }) {
                Icon(Icons.Default.RotateRight, contentDescription = null)
            }
            IconButton(onClick = { reloadTrigger++ }) {
                Icon(Icons.Default.Autorenew, contentDescription = null)
            }
            IconButton(onClick = { isFullscreen = true }) {
                Icon(Icons.Default.Fullscreen, contentDescription = null)
            }
        }
    }

    if (isFullscreen) {
        Dialog(onDismissRequest = { isFullscreen = false }) {
            FullScreenImageViewer(
                imageUrl = imageUrl,
                onDismiss = { isFullscreen = false },
                onPrevious = onPrevious,
                onNext = onNext,
                showNavigation = showNavigation
            )
        }
    }
}

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    showNavigation: Boolean
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    var composableSize by remember { mutableStateOf(IntSize.Zero) }
    var reloadTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(imageUrl) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        rotation = 0f
        reloadTrigger = 0
    }

    fun resetTransform() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { composableSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale <= 1f) scale = 2.4f
                        else resetTransform()
                    }
                )
            }
            .pointerInput(imageUrl) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val oldScale = scale
                    scale *= zoom
                    scale = scale.coerceIn(1f, 5f)

                    if (scale > 1f) {
                        val maxOffsetX = (composableSize.width * (scale - 1)) / 2f
                        val maxOffsetY = (composableSize.height * (scale - 1)) / 2f

                        offsetX = (offsetX + pan.x * oldScale).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + pan.y * oldScale).coerceIn(-maxOffsetY, maxOffsetY)
                    } else resetTransform()
                }
            }
    ) {
        AsyncImage(
            model = "$imageUrl?reload=$reloadTrigger",
            contentDescription = null,
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

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.FullscreenExit, null, tint = Color.White)
        }
    }
}
