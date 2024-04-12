package fr.isen.mollinari.androidsmartdevice

import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.isen.mollinari.androidsmartdevice.composable.CustomToolBar
import fr.isen.mollinari.androidsmartdevice.composable.ScanComposable
import fr.isen.mollinari.androidsmartdevice.composable.ScanComposableInteraction
import fr.isen.mollinari.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var mScanning by remember { mutableStateOf(false) }
            val scanInteraction = ScanComposableInteraction(
                isScanning = mScanning,
                arrayListOf<ScanResult>(),
                playPauseAction = {
                    mScanning = !mScanning
                    togglePlayPauseAction()
                }
            )

            AndroidSmartDeviceTheme {
               CustomToolBar(content = {
                   ScanComposable(scanInteraction)
                })
            }
        }
    }

    private fun togglePlayPauseAction() {
        Toast.makeText(this, "vous avez cliqué", Toast.LENGTH_SHORT).show()
    }
}
