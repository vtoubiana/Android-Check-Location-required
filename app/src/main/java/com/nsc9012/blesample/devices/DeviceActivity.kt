package com.nsc9012.blesample.devices

//import android.bluetooth.le.BluetoothLeScanner
//import android.bluetooth.le.ScanCallback
//import android.bluetooth.le.ScanResult


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.ParcelUuid
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.nsc9012.blesample.R
import com.nsc9012.blesample.extensions.invisible
import com.nsc9012.blesample.extensions.toast
import com.nsc9012.blesample.extensions.visible
import kotlinx.android.synthetic.main.activity_devices.*
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*
import kotlin.experimental.and


class DeviceActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
        const val BLE_SERVICE_UUID: String = "0000fd64-0000-1000-8000-00805f9b34fb"
        const val BLE_CHARACTERISTIC_UUID: String = "a8f12d00-ee67-478b-b95f-65d599407756"
        const val TT_service_UUID : String = "B82AB3FC-1595-4F6A-80F0-FE094CC218F9"
        const val EN_service_id : String = "0000fd6f-0000-1000-8000-00805f9b34fb";

       // val CONTACT_TRACER_UUID = ParcelUuid(0xFD6F)

        const val BLE_BACKGROUND_SERVICE_MANUFACTURER_DATA_IOS: String = "1.0.0.0.0.0.0.0.0.0.0.8.0.0.0.0.0"

    }
    //val CONTACT_TRACER_UUID = ParcelUuid(to128BitUuid(0xFD6F.toShort()))
    private val BIT_INDEX_OF_16_BIT_UUID: Int = 32
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScannerCompat

    private val deviceListAdapter = DevicesAdapter()



    private fun to128BitUuid(shortUuid: Short): UUID? {
        val baseUuid = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB")
        return UUID(
            ((shortUuid  and 0xFFFFL.toShort()) shl BIT_INDEX_OF_16_BIT_UUID).toLong() or baseUuid.mostSignificantBits,
            baseUuid.leastSignificantBits
        )
    }

    /* Listen for scan results */
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            deviceListAdapter.addDevice(result.device)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                deviceListAdapter.addDevice(result.device)
            }
            super.onBatchScanResults(results)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        initBLE()
        initUI()
    }

    private fun initUI() {

        title = getString(R.string.ble_scanner)

        recycler_view_devices.adapter = deviceListAdapter
        recycler_view_devices.layoutManager = LinearLayoutManager(this)
        recycler_view_devices.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        button_discover.setOnClickListener {
            with(button_discover) {
                if (text == getString(R.string.start_scanning)) {
                    deviceListAdapter.clearDevices()
                    text = getString(R.string.stop_scanning)
                    progress_bar.visible()
                    startScanning()
                } else {
                    text = getString(R.string.start_scanning)
                    progress_bar.invisible()
                    stopScanning()
                }

            }
        }
        button_discover3.setOnClickListener {
                with(button_discover3) {
                if (text == getString(R.string.start_scanning_filter)) {
                    deviceListAdapter.clearDevices()
                    text = getString(R.string.stop_scanning_filter)
                    progress_bar.visible()
                    startFilteredScanning()
                }else {
                    text = getString(R.string.start_scanning_filter)
                    progress_bar.invisible()
                    stopScanning()
                }
            }
        }

    }



    private fun initBLE() {

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = BluetoothLeScannerCompat.getScanner()

        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_COARSE_LOCATION
            )
        }

    }

    

    private fun startScanning() {
        val settings = ScanSettings.Builder()
            .setLegacy(true)
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setReportDelay(1000)
            .setUseHardwareBatchingIfSupported(true)
            .setUseHardwareFilteringIfSupported(true)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .build()

         AsyncTask.execute { bluetoothLeScanner.startScan(null, settings,leScanCallback) }
    }

    private fun startFilteredScanning() {
        val settings = ScanSettings.Builder()
            .setLegacy(true)
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setReportDelay(1000)
            .setUseHardwareBatchingIfSupported(true)
            .setUseHardwareFilteringIfSupported(true)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .build()
        val filters =buildScanFilter()

        AsyncTask.execute { bluetoothLeScanner.startScan(filters, settings,leScanCallback) }
    }

    private fun buildScanFilter(): List<ScanFilter> {
        return listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString(EN_service_id))).build(),
            ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString(BLE_SERVICE_UUID))
                ).build(),
            ScanFilter.Builder()
                .setServiceUuid(null)
                .setManufacturerData(
                    76,
                    BLE_BACKGROUND_SERVICE_MANUFACTURER_DATA_IOS.toByteArray()
                ).build(),
            ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString(TT_service_UUID))).build()
        )
    }


    private fun stopScanning() {
        AsyncTask.execute { bluetoothLeScanner.stopScan(leScanCallback) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted
                } else {
                    toast("Without location access, this app cannot discover beacons.")
                }
            }
        }
    }
}

private infix fun Short.shl(b: Int) = (toInt() shl b).toShort()
