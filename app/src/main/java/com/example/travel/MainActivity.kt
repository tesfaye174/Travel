package com.example.travel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.travel.ui.screens.AddTripScreen
import com.example.travel.ui.screens.JourneyTrackingScreen
import com.example.travel.ui.screens.StatisticsScreen
import com.example.travel.viewmodel.JourneyViewModel
import com.example.travel.viewmodel.TripViewModel
import com.example.travel.data.Trip
import com.example.travel.ui.screens.JourneyMapScreen

import android.content.Intent
import android.content.Context
import android.content.IntentFilter
import android.content.BroadcastReceiver
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.travel.worker.NotificationWorker
import java.util.concurrent.TimeUnit
import com.example.travel.service.ActivityRecognitionService
import com.google.android.gms.location.DetectedActivity

class MainActivity : ComponentActivity() {
    private val tripViewModel: TripViewModel by viewModels {
        TripViewModel.TripViewModelFactory((application as TravelApplication).database.tripDao(), (application as TravelApplication).database.journeyDao())
    }
    private val journeyViewModel: JourneyViewModel by viewModels {
        JourneyViewModel.JourneyViewModelFactory(
            (application as TravelApplication).database.journeyDao(),
            (application as TravelApplication).database.photoDao(),
            (application as TravelApplication).database.noteDao()
        )
    }

    private val activityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ActivityRecognitionService.ACTION_ACTIVITY_DETECTED) {
                val activityType = intent.getIntExtra(ActivityRecognitionService.EXTRA_ACTIVITY_TYPE, DetectedActivity.UNKNOWN)
                val confidence = intent.getIntExtra(ActivityRecognitionService.EXTRA_ACTIVITY_CONFIDENCE, 0)

                when (activityType) {
                    DetectedActivity.WALKING, DetectedActivity.RUNNING, DetectedActivity.ON_BICYCLE, DetectedActivity.IN_VEHICLE -> {
                        // Simulate starting a journey. In a real app, you'd need a tripId.
                        // For demonstration, let's assume a trip with ID 1 exists or create a default one.
                        // journeyViewModel.startNewJourney(1L) // Uncomment and provide a valid tripId
                        println("Auto-detected active state: $activityType with confidence $confidence. Journey would start.")
                    }
                    DetectedActivity.STILL -> {
                        // Simulate stopping a journey.
                        // journeyViewModel.stopCurrentJourney()
                        println("Auto-detected still state: $activityType with confidence $confidence. Journey would stop.")
                    }
                    else -> {
                        println("Auto-detected other activity: $activityType with confidence $confidence")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the BroadcastReceiver
        val filter = IntentFilter(ActivityRecognitionService.ACTION_ACTIVITY_DETECTED)
        registerReceiver(activityReceiver, filter)

        // Schedule periodic notification worker
        val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "TravelNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWorkRequest
        )

        setContent {
            TravelTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "tripList") {
                    composable("tripList") {
                        TripListScreen(tripViewModel = tripViewModel, navController = navController)
                    }
                    composable("addTrip") {
                        AddTripScreen(navController = navController, tripViewModel = tripViewModel)
                    }
                    composable(
                        "journeyTracking/{tripId}",
                        arguments = listOf(navArgument("tripId") { type = NavType.LongType })
                    ) {
                        val tripId = it.arguments?.getLong("tripId")
                        if (tripId != null) {
                            JourneyTrackingScreen(navController = navController, journeyViewModel = journeyViewModel, tripId = tripId)
                        } else {
                            // Handle error or navigate back
                            Text("Error: Trip ID not found")
                        }
                    }
                    composable(
                        "journeyMap/{journeyId}",
                        arguments = listOf(navArgument("journeyId") { type = NavType.LongType })
                    ) {
                        val journeyId = it.arguments?.getLong("journeyId")
                        if (journeyId != null) {
                            JourneyMapScreen(navController = navController, journeyViewModel = journeyViewModel, journeyId = journeyId)
                        } else {
                            Text("Error: Journey ID not found")
                        }
                    }
                    composable("statistics") {
                        StatisticsScreen(tripViewModel = tripViewModel)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(activityReceiver)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(tripViewModel: TripViewModel, navController: NavController) {
    val trips by tripViewModel.trips.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel Companion") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addTrip") }) {
                Icon(Icons.Filled.Add, "Add new trip")
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Button(onClick = { navController.navigate("statistics") }, modifier = Modifier.padding(16.dp)) {
                    Text("View Statistics")
                }
                Button(onClick = {
                    val intent = Intent(context, ActivityRecognitionService::class.java)
                    context.startService(intent)
                }, modifier = Modifier.padding(16.dp)) {
                    Text("Start Activity Recognition")
                }
                Button(onClick = {
                    val intent = Intent(context, ActivityRecognitionService::class.java)
                    context.stopService(intent)
                }, modifier = Modifier.padding(16.dp)) {
                    Text("Stop Activity Recognition")
                }
                if (trips.isEmpty()) {
                    Text("No trips planned yet. Add a new trip!", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(trips) { trip ->
                            TripItem(trip = trip, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripItem(trip: Trip, navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Destination: ${trip.destination}", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Dates: ${trip.startDate} - ${trip.endDate}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Type: ${trip.type}", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { navController.navigate("journeyTracking/${trip.id}") }) {
            Text("Start Journey")
        }
        Button(onClick = { navController.navigate("tripJourneys/${trip.id}") }) {
            Text("View Journeys")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TravelTheme {
        Text("Preview of TravelApp")
    }
}
