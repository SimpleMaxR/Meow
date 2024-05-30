package com.hugo.meow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hugo.meow.data.NetworkService
import com.hugo.meow.model.MeowPicture

@Composable
fun MeowApp(meowViewModel: MeowViewModel) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MarsTopAppBar() }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                meowUiState = meowViewModel.meowUiState,
                meowPics = meowViewModel.meowPics,
                onRefresh = { meowViewModel.getMeowPhotosAndLoadAll() },
                onLoadLocal = { meowViewModel.loadAllPhotosFromLocal() }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    meowUiState: MeowUiState,
    meowPics: List<MeowPicture>,
    onRefresh: () -> Unit,
    onLoadLocal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { onRefresh() }) {
            Text(text = "Refresh")
        }
        Button(onClick = { onLoadLocal() }) {
            Text(text = "Load from Local")
        }
        // 这里可以添加显示图片的代码，例如 LazyColumn 或 LazyRow
        when (meowUiState) {
            is MeowUiState.Loading -> {
                CircularProgressIndicator()
            }
            is MeowUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(meowPics) { meowPicture ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(meowPicture.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = meowPicture.id,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .sizeIn(
                                    minWidth = 100.dp,
                                    maxWidth = 150.dp,
                                    minHeight = 130.dp,
                                    maxHeight = 170.dp
                                ) // 设置大小范围
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
            is MeowUiState.Error -> {
                Text(text = "Error loading data")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarsTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Meow",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        modifier = modifier
    )
}