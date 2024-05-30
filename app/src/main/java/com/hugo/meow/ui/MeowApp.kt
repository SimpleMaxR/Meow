package com.hugo.meow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
                viewModel = meowViewModel
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: MeowViewModel
) {
    val meowUiState = viewModel.meowUiState
    val meowPics = viewModel.meowPics

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Column {
            Button(onClick = { viewModel.getMeowPhotosAndLoadAll() }) {
                Text(text = "Refresh")
            }
            Row() {
                Button(onClick = { viewModel.loadAllPhotosFromLocal() }) {
                    Text(text = "Load from Local")
                }
                Button(onClick = { viewModel.CleanDatabase() }) {
                    Text(text = "Clean！！！！")
                }
            }
        }

        // 根据 [meowUiState] 的状态切换不同显示内容
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
                                .data(meowPicture.path)
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
        modifier = modifier.background(color = Color.Blue)
    )
}