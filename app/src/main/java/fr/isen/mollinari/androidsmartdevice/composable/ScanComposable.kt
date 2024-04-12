package fr.isen.mollinari.androidsmartdevice.composable

import android.bluetooth.le.ScanResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
            contentDescription = ""
        )
    }
    if (scanInteraction.isScanning) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 24.dp),
        )
        LazyColumn {
            items(arrayListOf("test 1", "test 2")) {
                Text(text = it)
            }
        }
    } else {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 24.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

private fun getScanTitle(isScanning: Boolean): Int =
    if(isScanning) {
        R.string.ble_scan_title_pause
    } else {
        R.string.ble_scan_title_play
    }

private fun getScanImage(isScanning: Boolean): Int =
    if(isScanning) {
        R.drawable.baseline_pause_24
    } else {
        R.drawable.baseline_play_arrow_24
    }

class ScanComposableInteraction(
    val isScanning: Boolean,
    val scanResults: ArrayList<ScanResult>,
    val playPauseAction: () -> Unit
)