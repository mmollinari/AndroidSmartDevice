package fr.isen.mollinari.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import fr.isen.mollinari.androidsmartdevice.ScanActivity.Companion.DEVICE_PARAM
import fr.isen.mollinari.androidsmartdevice.composable.CustomToolBar
import fr.isen.mollinari.androidsmartdevice.composable.DeviceComposable
import fr.isen.mollinari.androidsmartdevice.composable.DeviceComposableInteraction
import fr.isen.mollinari.androidsmartdevice.composable.Led
import fr.isen.mollinari.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import java.util.Locale

@SuppressLint("MissingPermission")
class DeviceActivity : ComponentActivity() {

    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var deviceInteraction : DeviceComposableInteraction
    private var currentLEDStateEnum = LEDStateEnum.NONE

    private var ledBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    private var counterBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    private var controlBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val device = intent.getParcelableExtra<BluetoothDevice?>(DEVICE_PARAM)

        setContent {
            val isStateConnected = remember { mutableStateOf(false) }
            val notificationCounter = remember { mutableStateOf("0") }
            val controlCounter = remember { mutableStateOf("0") }
            val ledState1 = remember { mutableStateOf(false) }
            val ledState2 = remember { mutableStateOf(false) }
            val ledState3 = remember { mutableStateOf(false) }
            val ledArray = listOf(
                Led(ledState1) { turnOnLight(0, LEDStateEnum.LED_1) },
                Led(ledState2) { turnOnLight(1, LEDStateEnum.LED_2) },
                Led(ledState3) { turnOnLight(2, LEDStateEnum.LED_3) },
            )

            deviceInteraction = DeviceComposableInteraction(
                isConnected = isStateConnected,
                deviceTitle = device?.name ?: "Device Unknown",
                ledArray = ledArray,
                notificationCounter = notificationCounter,
                onNotificationSubscribe = { isChecked ->
                    toggleNotification(counterBluetoothGattCharacteristic, isChecked)
                },
                controlCounter = controlCounter,
                onControlSubscribe = { isChecked ->
                    toggleNotification(controlBluetoothGattCharacteristic, isChecked)
                }
            )

            AndroidSmartDeviceTheme {
                CustomToolBar(content = {
                    DeviceComposable(deviceInteraction)
                })
            }

            connectToDevice(device)
        }
    }

    private fun connectToDevice(device: BluetoothDevice?) {
        bluetoothGatt = device?.connectGatt(this, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                connectionStateChange(gatt, newState)
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                val bleServices =
                    gatt?.services
                ledBluetoothGattCharacteristic = bleServices?.get(2)?.characteristics?.get(0)
                counterBluetoothGattCharacteristic = bleServices?.get(2)?.characteristics?.get(1)
                controlBluetoothGattCharacteristic = bleServices?.get(3)?.characteristics?.get(0)
            }

            @Deprecated("Deprecated for Android 13+")
            @Suppress("DEPRECATION")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                onGlobalCharacteristicChanged(characteristic, characteristic.value)

            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                super.onCharacteristicChanged(gatt, characteristic, value)
                onGlobalCharacteristicChanged(characteristic, value)
            }
        })
        bluetoothGatt?.connect()
    }

    private fun connectionStateChange(gatt: BluetoothGatt?, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt?.discoverServices()
        }
        runOnUiThread {
            deviceInteraction.isConnected.value = newState == BluetoothProfile.STATE_CONNECTED
        }
    }

    private fun turnOnLight(index: Int, newLedState: LEDStateEnum) {
        if(currentLEDStateEnum != newLedState) {
            deviceInteraction.ledArray.forEach { it.isOn.value = false }
            deviceInteraction.ledArray[index].isOn.value = !deviceInteraction.ledArray[index].isOn.value
            currentLEDStateEnum = newLedState
        } else {
            deviceInteraction.ledArray.forEach { it.isOn.value = false }
            currentLEDStateEnum = LEDStateEnum.NONE
        }
        writeIntoLEDCharacteristic(currentLEDStateEnum)
    }

    private fun writeIntoLEDCharacteristic(newLedState: LEDStateEnum) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ledBluetoothGattCharacteristic?.let {
                bluetoothGatt?.writeCharacteristic(it, newLedState.hex, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            }
        } else {
            if(ledBluetoothGattCharacteristic != null) {
                ledBluetoothGattCharacteristic?.value = newLedState.hex
                bluetoothGatt?.writeCharacteristic(ledBluetoothGattCharacteristic)
            } else {
                Toast.makeText(this, "Characteristique indisponible", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onGlobalCharacteristicChanged(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val hex = value.joinToString("") { byte -> "%02x".format(byte)}.uppercase(Locale.FRANCE)
        Log.d("hex", hex)
        runOnUiThread {
            if(characteristic.uuid === counterBluetoothGattCharacteristic?.uuid) {
                deviceInteraction.notificationCounter.value = hex
            } else if (characteristic.uuid === controlBluetoothGattCharacteristic?.uuid) {
                deviceInteraction.controlCounter.value = hex
            }
        }
    }

    private fun toggleNotification(
        bluetoothGattCharacteristic: BluetoothGattCharacteristic?,
        isChecked: Boolean
    ) {
        bluetoothGatt?.setCharacteristicNotification(bluetoothGattCharacteristic, isChecked)
        bluetoothGattCharacteristic?.descriptors?.forEach {
            it.value =
                if (isChecked) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(it)
        }
        Log.d("value", bluetoothGattCharacteristic?.value?.joinToString("") { byte -> "%02x".format(byte)}?.uppercase(Locale.FRANCE) ?: "nulll ...")
    }

    override fun onStop() {
        super.onStop()
        closeBluetoothGatt()
    }

    private fun closeBluetoothGatt() {
        deviceInteraction.isConnected.value = false
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}

