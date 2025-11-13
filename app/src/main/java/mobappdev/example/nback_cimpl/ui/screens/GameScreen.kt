package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.FeedbackType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    vm: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()

    val buttonScale by animateFloatAsState(
        targetValue = when (gameState.feedback) {
            FeedbackType.CORRECT -> 1.1f
            FeedbackType.INCORRECT -> 0.9f
            FeedbackType.NONE -> 1.0f
        },
        label = "buttonScale"
    )

    val letters = listOf("A", "B", "C", "D", "E", "F", "G", "H")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("N-Back (N=${vm.nBack})") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Score", style = MaterialTheme.typography.labelMedium)
                        Text("$score", style = MaterialTheme.typography.headlineMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Event", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${gameState.eventIndex + 1}/${vm.nrOfEvents}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Correct", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${gameState.correctCount}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display based on game type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (gameState.gameType) {
                    GameType.Visual -> {
                        // Visual grid
                        Column(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (row in 0 until vm.gridSize) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (col in 0 until vm.gridSize) {
                                        val position = row * vm.gridSize + col + 1
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize()
                                                .background(
                                                    color = if (position == gameState.eventValue) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    },
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    GameType.Audio -> {
                        // Audio display
                        Card(
                            modifier = Modifier
                                .size(200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (gameState.audioValue > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (gameState.audioValue > 0) {
                                        letters[gameState.audioValue - 1]
                                    } else {
                                        "🔊"
                                    },
                                    style = MaterialTheme.typography.displayLarge
                                )
                            }
                        }
                    }
                    GameType.AudioVisual -> {
                        // Dual mode - split screen
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Visual half
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (row in 0 until vm.gridSize) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        for (col in 0 until vm.gridSize) {
                                            val position = row * vm.gridSize + col + 1
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                                    .background(
                                                        color = if (position == gameState.eventValue) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceVariant
                                                        },
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            // Audio half
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (gameState.audioValue > 0) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (gameState.audioValue > 0) {
                                            letters[gameState.audioValue - 1]
                                        } else {
                                            "🔊"
                                        },
                                        style = MaterialTheme.typography.displayLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { vm.checkMatch() },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp)
                    .scale(buttonScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (gameState.feedback) {
                        FeedbackType.CORRECT -> Color(0xFF4CAF50)
                        FeedbackType.INCORRECT -> Color(0xFFE53935)
                        FeedbackType.NONE -> MaterialTheme.colorScheme.secondary
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "MATCH!",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}