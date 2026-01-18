package com.feelingmindful.awareness.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelingmindful.awareness.domain.entities.Mantra
import com.feelingmindful.awareness.domain.entities.WindowType
import com.feelingmindful.awareness.ui.theme.AwarenessByChrisShanksTheme

@Composable
fun MantraCard(
    mantra: Mantra?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onRefresh),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoading) "..." else (mantra?.text ?: ""),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            mantra?.attribution?.let { attribution ->
                Text(
                    text = "— $attribution",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text = "Tap for a new mantra",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun MantraCardPreview() {
    AwarenessByChrisShanksTheme {
        MantraCard(
            mantra = Mantra(
                id = "1",
                text = "Be present in this moment.",
                windowType = WindowType.MORNING,
                attribution = "Thich Nhat Hanh"
            ),
            isLoading = false,
            onRefresh = {}
        )
    }
}
