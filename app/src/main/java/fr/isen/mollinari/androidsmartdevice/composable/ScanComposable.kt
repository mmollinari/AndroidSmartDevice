package fr.isen.mollinari.androidsmartdevice.composable

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.mollinari.androidsmartdevice.R

@Composable
fun ScanComposable(
    scanInteraction: ScanComposableInteraction
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { scanInteraction.playPauseAction() }
    ) {
        Text(
            text = stringResource(id = getScanTitle(scanInteraction.isScanning)),
            fontSize = 28.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(8.dp))
        Image(
            painter = painterResource(id = getScanImage(scanInteraction.isScanning)),
            contentDescription = "",
            Modifier.size(38.dp)
        )
    }
    if (scanInteraction.isScanning) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 24.dp, 0.dp, 0.dp),
        )
    } else {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 24.dp, 0.dp, 0.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
    LazyColumn(Modifier.fillMaxWidth()) {
        items(scanInteraction.scanResults) {
            DeviceComposable(it, scanInteraction)
        }
    }
}
@SuppressLint("MissingPermission")
@Composable
private fun DeviceComposable(it: ScanResult, scanInteraction: ScanComposableInteraction) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { scanInteraction.onDeviceClicked(it.device) }
            .alpha(convertRSSIToAlpha(it.rssi))
    ) {
        Text(
            text = it.rssi.toString(),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            color = Color.White,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(text = it.device.name ?: "Device Unknown")
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = it.device.address)
        }
    }

    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.primary)
    )
}

private fun getScanTitle(isScanning: Boolean): Int =
    if (isScanning) {
        R.string.ble_scan_title_pause
    } else {
        R.string.ble_scan_title_play
    }

private fun getScanImage(isScanning: Boolean): Int =
    if (isScanning) {
        R.drawable.baseline_pause_24
    } else {
        R.drawable.baseline_play_arrow_24
    }

private fun convertRSSIToAlpha(rssi: Int): Float =
    when {
        rssi > -40 -> 1f
        rssi > -100 -> (0.01 * rssi + 1.4).toFloat()
        else -> 0.4f
    }

class ScanComposableInteraction(
    var isScanning: Boolean,
    var hasBLEIssue: String = "",
    val scanResults: MutableList<ScanResult>,
    val playPauseAction: () -> Unit,
    val onDeviceClicked: (BluetoothDevice) -> Unit
)
