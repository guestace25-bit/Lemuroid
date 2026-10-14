package com.swordfish.lemuroid.corruptor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.swordfish.libretrodroid.LibretroDroid

class RomCorruptorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Seed the pristine buffer from the game's ROM if we have a path.
        val romPath = intent.getStringExtra("rom_path")
        if (RomCorruptor.pristine == null && romPath != null) {
            try {
                RomCorruptor.setSource(java.io.File(romPath).readBytes())
            } catch (_: Throwable) {
                // leave pristine null; UI shows "Unknown"
            }
        }

        setContent {
            MaterialTheme {
                RomCorruptorScreen(
                    onRecorrupt = { bytes ->
                        LibretroDroid.loadGameFromBytes(bytes)
                        LibretroDroid.reset()
                    },
                    onReset = { bytes ->
                        LibretroDroid.loadGameFromBytes(bytes)
                        LibretroDroid.reset()
                    },
                    onClose = { finish() },
                )
            }
        }
    }
}
