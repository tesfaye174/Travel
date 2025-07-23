package com.example.travel.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.travel.viewmodel.JourneyViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyMapScreen(navController: NavController, journeyViewModel: JourneyViewModel, journeyId: Long) {
    val context = LocalContext.current
    var journeyCoordinates by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f) // Default position
    }

    LaunchedEffect(journeyId) {
        val journey = journeyViewModel.getJourneyById(journeyId)
        journey?.let { j ->
            val coords = j.routeCoordinates.split(",").mapNotNull { coordString ->
                val parts = coordString.split(",")
                if (parts.size == 2) {
                    try {
                        LatLng(parts[0].toDouble(), parts[1].toDouble())
                    } catch (e: NumberFormatException) {
                        null
                    }
                } else {
                    null
                }
            }
            journeyCoordinates = coords
            if (coords.isNotEmpty()) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(coords.first(), 15f)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journey Map") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (journeyCoordinates.isNotEmpty()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Polyline(points = journeyCoordinates)
                }
            } else {
                Text("No coordinates available for this journey.", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
