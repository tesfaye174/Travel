package com.example.travel.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel.viewmodel.TripViewModel
import com.yml.charts.common.model.Point
import com.yml.charts.ui.barchart.BarChart
import com.yml.charts.ui.barchart.models.BarChartData
import com.yml.charts.ui.barchart.models.BarData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(tripViewModel: TripViewModel) {
    val trips by tripViewModel.trips.collectAsState()
    val tripsPerMonth by tripViewModel.tripsPerMonth.collectAsState()
    val totalDistance by tripViewModel.totalDistance.collectAsState()
    val tripsByType by tripViewModel.tripsByType.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel Statistics") },
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
            Text(text = "Total Trips: ${trips.size}")
            Text(text = "Total Distance: %.2f meters".format(totalDistance))
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Trips by Type:")
            tripsByType.forEach { (type, count) ->
                Text(text = "- $type: $count")
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (tripsPerMonth.isNotEmpty()) {
                val barData = tripsPerMonth.entries.sortedBy { it.key }.mapIndexed { index, entry ->
                    BarData(
                        point = Point(index.toFloat(), entry.value.toFloat()),
                        label = entry.key
                    )
                }
                BarChart(
                    barChartData = BarChartData(barData),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(top = 16.dp)
                )
            } else {
                Text("No monthly trip data available.", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
