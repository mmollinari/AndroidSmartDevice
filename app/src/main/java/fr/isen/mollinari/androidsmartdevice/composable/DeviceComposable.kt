package fr.isen.mollinari.androidsmartdevice.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.mollinari.androidsmartdevice.R

@Composable
fun DeviceComposable(deviceInteraction: DeviceComposableInteraction) {
    if (deviceInteraction.isConnected.value) {
        DeviceConnected(deviceInteraction)
    } else {
        DeviceNotConnect()
    }
}

@Composable
fun DeviceNotConnect() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Connecting to device ...",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.size(32.dp))
        CircularProgressIndicator()
    }
}

@Composable
fun DeviceConnected(deviceInteraction: DeviceComposableInteraction) {
    var checkedStateNotification by remember { mutableStateOf(false) }
    var checkedStateControl by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp, 0.dp)
    ) {
        Text(
            text = deviceInteraction.deviceTitle,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(30.dp))
        Text(text = stringResource(id = R.string.device_led_text))
        LazyRow {
            itemsIndexed(deviceInteraction.ledArray) { index, it ->
                Image(
                    painter = painterResource(id = R.drawable.led),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(color = getColorTintFromLed(it)),
                    modifier = Modifier
                        .size(80.dp)
                        .alpha(getAlphaFromLed(it))
                        .padding(8.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { it.switchLed(index) }
                )
            }
        }
        Spacer(modifier = Modifier.size(30.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.device_notification_subscribe_text),
                Modifier.fillMaxWidth(0.66f)
            )
            CheckBoxWithTextRippleFullRow(
                label = stringResource(id = R.string.device_subscribe),
                state = checkedStateNotification,
                onStateChange = {
                    checkedStateNotification = !checkedStateNotification
                    deviceInteraction.onNotificationSubscribe(checkedStateNotification)
                }
            )
        }
        Spacer(modifier = Modifier.size(30.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.device_counter_text))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = deviceInteraction.notificationCounter.value)
        }
        Spacer(modifier = Modifier.size(30.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.device_counter_subscribe_text),
                Modifier.fillMaxWidth(0.66f)
            )
            CheckBoxWithTextRippleFullRow(
                label = stringResource(id = R.string.device_subscribe),
                state = checkedStateControl,
                onStateChange = {
                    checkedStateControl = !checkedStateControl
                    deviceInteraction.onControlSubscribe(checkedStateControl)
                }
            )
        }
        Spacer(modifier = Modifier.size(30.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.device_counter_text))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = deviceInteraction.controlCounter.value)
        }
    }
}

@Composable
fun CheckBoxWithTextRippleFullRow(
    label: String,
    state: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
        .clickable(
            role = Role.Checkbox,
            onClick = {
                onStateChange(!state)
            }
        )
        .padding(8.dp)
    ) {
        Checkbox(
            checked = state,
            onCheckedChange = null
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = label, fontSize = 12.sp)
    }
}

@Composable
private fun getColorTintFromLed(led: Led) =
    if (led.isOn.value) MaterialTheme.colorScheme.primary else Color.Black

private fun getAlphaFromLed(led: Led) =
    if (led.isOn.value) 1f else 0.3f

class DeviceComposableInteraction(
    var isConnected: MutableState<Boolean>,
    var deviceTitle: String = "",
    val ledArray: List<Led>,
    var notificationCounter: MutableState<String>,
    val onNotificationSubscribe: (Boolean) -> Unit,
    var controlCounter: MutableState<String>,
    val onControlSubscribe: (Boolean) -> Unit
)

data class Led(var isOn: MutableState<Boolean>, val switchLed: (index: Int) -> Unit)
