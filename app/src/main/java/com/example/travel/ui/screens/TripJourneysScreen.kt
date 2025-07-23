package com.example.travel.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.travel.data.Journey
import com.example.travel.viewmodel.JourneyViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripJourneysScreen(navController: NavController, journeyViewModel: JourneyViewModel, tripId: Long) {
    var journeys by remember { mutableStateOf<List<Journey>>(emptyList()) }

    LaunchedEffect(tripId) {
        journeys = journeyViewModel.getJourneysForTrip(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journeys for Trip") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (journeys.isEmpty()) {
                Text("No journeys recorded for this trip.")
            } else {
                LazyColumn {
                    items(journeys) { journey ->
                        JourneyItem(journey = journey) {
                            navController.navigate("journeyMap/${journey.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JourneyItem(journey: Journey, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(text = "Start: ${dateFormat.format(journey.startTime)}")
            Text(text = "End: ${dateFormat.format(journey.endTime)}")
            Text(text = "Duration: ${journey.duration / 1000 / 60} minutes")
            Text(text = "Distance: %.2f meters".format(journey.distance))
        }
    }
}
