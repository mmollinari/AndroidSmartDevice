package fr.isen.mollinari.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import fr.isen.mollinari.androidsmartdevice.composable.CustomToolBar
import fr.isen.mollinari.androidsmartdevice.composable.ScanComposable
import fr.isen.mollinari.androidsmartdevice.composable.ScanComposableInteraction
import fr.isen.mollinari.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {

    private lateinit var handler: Handler
    private lateinit var scanInteraction: ScanComposableInteraction

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.all { it.value }) {
                scanLeDevice(scanInteraction.isScanning) /// attention le !
            }
        }

    private val isBLEEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private var permissionsList = arrayOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isScanning by remember { mutableStateOf(false) }
            val scanResults = remember {
                mutableStateListOf<ScanResult>()
            }
            scanInteraction = ScanComposableInteraction(
                isScanning = isScanning,
                scanResults = scanResults,
                playPauseAction = {
                    isScanning = !isScanning
                    scanLeDeviceWithPermission(isScanning)
                },
                onDeviceClicked = ::onDeviceClicked
            )

            AndroidSmartDeviceTheme {
                CustomToolBar(content = {
                    ScanComposable(scanInteraction)
                })
            }

            initBLEScan()
        }

    }

    private fun initBLEScan() {
        permissionsList = getAllPermissionsForBLE()
        if (isBLEEnabled) {
            handler = Handler(mainLooper)
            scanLeDeviceWithPermission(scanInteraction.isScanning)
        } else {
            handleSmartphoneBLEError()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBLEEnabled && isAllPermissionsGranted()) {
            scanLeDeviceWithPermission(false)
        }
    }

    private fun getAllPermissionsForBLE(): Array<String> {
        var allPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            allPermissions = allPermissions.plus(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            allPermissions = allPermissions.plus(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        return allPermissions
    }


    private fun handleSmartphoneBLEError() {
        val errorMessage = if (bluetoothAdapter != null) {
            getString(R.string.ble_scan_disabled)
        } else {
            getString(R.string.ble_scan_missing)
        }
        scanInteraction.hasBLEIssue = errorMessage
    }

    private fun onDeviceClicked(device: BluetoothDevice) {
        val intent = Intent(this@ScanActivity, DeviceActivity::class.java)
        intent.putExtra(DEVICE_PARAM, device)
        startActivity(intent)
    }

    private fun scanLeDeviceWithPermission(enable: Boolean) {
        if (isAllPermissionsGranted()) {
            scanLeDevice(enable)
        } else {
            requestPermissionLauncher.launch(permissionsList)
        }
    }

    private fun isAllPermissionsGranted() = permissionsList.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    fun addDeviceToList(result: ScanResult) {
        val index = scanInteraction.scanResults.indexOfFirst { it.device.address == result.device.address }
        if (index != -1) {
            scanInteraction.scanResults[index] = result
        } else {
            scanInteraction.scanResults.add(result)
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanLeDevice(enable: Boolean) {
        bluetoothAdapter?.bluetoothLeScanner?.apply {
            if (enable) {
                handler.postDelayed({
                    scanInteraction.isScanning = false
                    stopScan(leScanCallback)
                }, SCAN_PERIOD)
                scanInteraction.isScanning = true
                startScan(leScanCallback)
                scanInteraction.scanResults.clear()
            } else {
                scanInteraction.isScanning = false
                stopScan(leScanCallback)
            }
        }
    }

    private val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.w(this@ScanActivity.localClassName, "${result.device}")
            if(!result.device.name.isNullOrEmpty()) {
                addDeviceToList(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(
                this@ScanActivity,
                getString(R.string.ble_scan_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        const val DEVICE_PARAM: String = "ble_device"
        private const val SCAN_PERIOD: Long = 20000
    }
}
