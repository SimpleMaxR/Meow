package com.hugo.meow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MeowApp(meowViewModel: MeowViewModel) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        topBar = { MeowTopAppBar() },
        floatingActionButton = {
            FloatingActionButton(onClick = { meowViewModel.getMeowPhotosAndLoadAll() }) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = null
                )
            }
        }
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            RequestCountInput(viewModel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.getMeowPhotosAndLoadAll() }) {
                    Text(text = "Refresh")
                }
                Button(onClick = { viewModel.loadAllPhotosFromLocal() }) {
                    Text(text = "Load from Local")
                }
                Button(onClick = { viewModel.CleanDatabase() }) {
                    Text(text = "Clean")
                }
            }
        }

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
                                )
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
            is MeowUiState.Error -> {
                Text(text = "Error loading data", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeowTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Meow 🐱",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
    )
}

@Composable
fun RequestCountInput(viewModel: MeowViewModel) {
    val requestCount by viewModel.requestCount.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = { viewModel.updateRequestCount(requestCount + 1) }) {
            Text(text = "More")
        }
        Text(
            text = requestCount.toString(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Button(onClick = { viewModel.updateRequestCount(requestCount - 1) }) {
            Text(text = "Less")
        }
    }
}