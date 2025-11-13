package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: GameViewModel,
    onNavigateBack: () -> Unit,
    onSaveSettings: (Int, Int, Long, Int) -> Unit
) {
    var nBackValue by remember { mutableStateOf(vm.nBack) }
    var eventCount by remember { mutableStateOf(vm.nrOfEvents) }
    var eventInterval by remember { mutableStateOf(vm.eventIntervalMs / 1000) }
    var gridSize by remember { mutableStateOf(3) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Game Configuration",
                style = MaterialTheme.typography.headlineMedium
            )

            // N-Back value
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "N-Back Value: $nBackValue",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = nBackValue.toFloat(),
                        onValueChange = { nBackValue = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    Text(
                        text = "Match positions from $nBackValue steps back",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Number of events
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Events per Round: $eventCount",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = eventCount.toFloat(),
                        onValueChange = { eventCount = it.toInt() },
                        valueRange = 5f..30f,
                        steps = 4
                    )
                }
            }

            // Event interval
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Time Between Events: ${eventInterval}s",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = eventInterval.toFloat(),
                        onValueChange = { eventInterval = it.toLong() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }
            }

            // Grid size
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Grid Size: ${gridSize}x${gridSize}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 4, 5).forEach { size ->
                            FilterChip(
                                selected = gridSize == size,
                                onClick = { gridSize = size },
                                label = { Text("${size}x${size}") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    onSaveSettings(nBackValue, eventCount, eventInterval * 1000, gridSize)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save Settings", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}