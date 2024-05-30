package com.hugo.meow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hugo.meow.data.NetworkService
import com.hugo.meow.model.MeowPicture

@Composable
fun MeowApp(networkService: NetworkService, meowViewModel: MeowViewModel) {

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
                onRefresh = {
                    meowViewModel.getMeowPhotos()
                    meowViewModel.loadAllPhotos()
                }
            )
        }
    }
}

@Composable
fun HomeScreen(meowUiState: MeowUiState, meowPics: List<MeowPicture>, onRefresh: () -> Unit) {
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
        // 这里可以添加显示图片的代码，例如 LazyColumn 或 LazyRow
        when (meowUiState) {
            is MeowUiState.Loading -> {
                CircularProgressIndicator()
            }

            is MeowUiState.Success -> {
                LazyColumn {
                    items(meowPics) { meowPicture ->
                        Text(text = meowPicture.url)
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