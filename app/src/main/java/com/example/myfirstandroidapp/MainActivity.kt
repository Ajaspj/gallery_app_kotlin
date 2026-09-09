package com.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.gallery.data.model.MediaItem
import com.gallery.ui.home.HomeScreen
import com.gallery.ui.home.MediaDetailScreen
import com.gallery.ui.home.SearchScreen
import com.gallery.ui.theme.GalleryTheme

sealed class Screen {
    object Home : Screen()
    object Search : Screen()
}

class MainActivity : ComponentActivity() {

    private var currentScreen by mutableStateOf<Screen>(Screen.Home)
    private var selectedMedia by mutableStateOf<Pair<List<MediaItem>, Int>?>(null)

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val granted = permissions.values.any { it }
            if (granted) {
                setupContent()
            } else {
                showPermissionScreen()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val hasPermission = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (hasPermission) {
            setupContent()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun setupContent() {
        setContent {
            GalleryTheme(darkTheme = false) {
                val media = selectedMedia
                if (media != null) {
                    BackHandler { selectedMedia = null }
                    MediaDetailScreen(
                        mediaItems = media.first,
                        initialIndex = media.second,
                        onBack = { selectedMedia = null }
                    )
                } else {
                    when (currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                onMediaClick = { list, index ->
                                    selectedMedia = list to index
                                },
                                onSearchClick = {
                                    currentScreen = Screen.Search
                                }
                            )
                        }
                        is Screen.Search -> {
                            BackHandler { currentScreen = Screen.Home }
                            SearchScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                onMediaClick = { list, index ->
                                    selectedMedia = list to index
                                },
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showPermissionScreen() {
        setContent {
            GalleryTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = { checkPermissions() }) {
                        Text(text = "Allow Media Access")
                    }
                }
            }
        }
    }
}