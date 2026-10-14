package com.swordfish.lemuroid.corruptor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RomCorruptorScreen(
    onRecorrupt: (ByteArray) -> Unit,
    onReset: (ByteArray) -> Unit,
    onClose: () -> Unit,
) {
    var enabled by remember { mutableStateOf(RomCorruptor.enabled) }
    var algo by remember { mutableStateOf(RomCorruptor.algorithm) }
    var intensity by remember { mutableStateOf(RomCorruptor.intensity.toFloat()) }
    var stride by remember { mutableStateOf(RomCorruptor.stride.toFloat()) }
    var protect by remember { mutableStateOf(RomCorruptor.headerProtect) }
    var status by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("ROM Corruptor", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Detected: ${RomCorruptor.platformName()}",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        RomCorruptor.enabled = it
                    },
                )
                Spacer(Modifier.width(8.dp))
                Text("Enable Corruption")
            }

            Text("Algorithm", style = MaterialTheme.typography.labelLarge)
            RomCorruptor.ALGORITHM_NAMES.forEachIndexed { idx, name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = algo == idx,
                        onClick = {
                            algo = idx
                            RomCorruptor.algorithm = idx
                        },
                    )
                    Text(name)
                }
            }

            Text("Intensity: ${intensity.toInt()}%")
            Slider(
                value = intensity,
                onValueChange = {
                    intensity = it
                    RomCorruptor.intensity = it.toInt()
                },
                valueRange = 0f..100f,
            )

            Text("Stride: every ${stride.toInt()} byte(s)")
            Slider(
                value = stride,
                onValueChange = {
                    stride = it
                    RomCorruptor.stride = it.toInt().coerceAtLeast(1)
                },
                valueRange = 1f..512f,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = protect,
                    onCheckedChange = {
                        protect = it
                        RomCorruptor.headerProtect = it
                    },
                )
                Text("Protect headers & vectors")
            }

            Button(
                onClick = {
                    RomCorruptor.corrupt()
                    RomCorruptor.mutated?.let(onRecorrupt)
                    status = "Corrupted ${RomCorruptor.mutated?.size ?: 0} bytes " +
                        "in ${RomCorruptor.lastCorruptMs} ms"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("RE-CORRUPT", style = MaterialTheme.typography.titleLarge)
            }

            OutlinedButton(
                onClick = {
                    RomCorruptor.pristine?.let(onReset)
                    status = "Restored pristine ROM"
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset ROM")
            }

            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Close")
            }

            if (status.isNotEmpty()) {
                Text(status, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
