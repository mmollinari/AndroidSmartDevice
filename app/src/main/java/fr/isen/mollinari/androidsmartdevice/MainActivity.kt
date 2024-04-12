package fr.isen.mollinari.androidsmartdevice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.mollinari.androidsmartdevice.composable.CustomToolBar
import fr.isen.mollinari.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                MainComponent(::goToScan)
            }
        }
    }

    private fun goToScan() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun MainComponent(action: () -> Unit) {
    CustomToolBar(verticalArrangement = Arrangement.SpaceBetween, content = {
        Column {
            Text(
                text = stringResource(R.string.main_title),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(30.dp))
            Text(
                text = stringResource(id = R.string.main_description),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(80.dp))
            Image(
                painter = painterResource(id = R.drawable.baseline_bluetooth_24),
                contentDescription = "BLE",
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally).size(180.dp)
            )
        }
        Button(onClick = { action() }, Modifier.padding(24.dp, 0.dp).fillMaxWidth()) {
            Text(text = stringResource(id = R.string.main_action))
        }
    })
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidSmartDeviceTheme {
        MainComponent {}
    }
}