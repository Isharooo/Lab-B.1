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
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    vm: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()

    // Animation för knapp-feedback
    val buttonScale by animateFloatAsState(
        targetValue = when (gameState.feedback) {
            FeedbackType.CORRECT -> 1.1f
            FeedbackType.INCORRECT -> 0.9f
            FeedbackType.NONE -> 1.0f
        },
        label = "buttonScale"
    )

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
            // Game info card
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

            // 3x3 Grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0..2) {
                            val position = row * 3 + col + 1
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

            // Match button med feedback
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