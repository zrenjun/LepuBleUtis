@file:OptIn(ExperimentalTime::class)

package com.lepu.ble

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.AndroidPeripheral
import com.juul.kable.Bluetooth
import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import com.juul.kable.State
import com.juul.kable.peripheral
import com.lepu.ble.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val reconnectDelay = 1.seconds

sealed class ViewState {

    data object BluetoothUnavailable : ViewState()

    data object Connecting : ViewState()

    data object Connected : ViewState()

    data object Disconnecting : ViewState()

    data object Disconnected : ViewState()
}


val ViewState.label: String
    get() = when (this) {
        ViewState.BluetoothUnavailable -> "蓝牙不可用"
        ViewState.Connecting -> "连接中"
        is ViewState.Connected -> "已连接"
        ViewState.Disconnecting -> "断开连接"
        ViewState.Disconnected -> "连接断开"
    }

val peripheralScope = CoroutineScope(Job())

class SensorViewModel(application: Application, mac: String) : AndroidViewModel(application) {

    private val autoConnect = MutableStateFlow(false)

    // Intermediary scope needed until https://github.com/JuulLabs/kable/issues/577 is resolved.
    private val scope =
        CoroutineScope(peripheralScope.coroutineContext + Job(peripheralScope.coroutineContext.job))

    private val peripheral = scope.peripheral(mac) { autoConnectIf(autoConnect::value) }
    private val bleDevice = BleDevice(peripheral)
    private val state = combine(Bluetooth.availability, peripheral.state, ::Pair)

    init {
        viewModelScope.enableAutoReconnect()
    }

    private fun CoroutineScope.enableAutoReconnect() {
        state.filter { (bluetoothAvailability, connectionState) ->
            bluetoothAvailability == Available && connectionState is State.Disconnected
        }.onEach {
            ensureActive()
            LogUtil.e("Waiting $reconnectDelay to reconnect...")
            delay(reconnectDelay)
            connect()
        }.launchIn(this)
    }

    private fun CoroutineScope.connect() {
        launch {
            LogUtil.e("Connecting")
            try {
                peripheral.connect()
                (peripheral as AndroidPeripheral).requestMtu(247)
                getRtWave().collectLatest {
                    LogUtil.e("getRtWave $it")
                }
                autoConnect.value = true
            } catch (e: Exception) {
                autoConnect.value = false
                LogUtil.e("Connection attempt failed")
            }
        }
    }


    private var startTime: TimeMark? = null


    var preBytes = byteArrayOf()

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState: Flow<ViewState> = state
        .flatMapLatest { (bluetoothAvailability, state) ->
            if (bluetoothAvailability is Unavailable) {
                return@flatMapLatest flowOf(ViewState.BluetoothUnavailable)
            }
            when (state) {
                is State.Connecting -> flowOf(ViewState.Connecting)
                State.Connected -> flowOf(ViewState.Connected)
                State.Disconnecting -> flowOf(ViewState.Disconnecting)
                is State.Disconnected -> flowOf(ViewState.Disconnected)
            }
        }

    private var temp = byteArrayOf()

    @OptIn(ExperimentalCoroutinesApi::class)
    val data = bleDevice.notify
        .filter {
            if ((it[0].toInt() and 0xff == 0x55) && it[1].toInt() == 0x00) {
                val len = it[5].toInt() and 0xff
                if (len > 20) {
                    temp += it
                }
                false
            } else {
                if (temp.isNotEmpty()) {
                    temp += it
                    true
                } else {
                    false
                }
            }
        }
        .map {
            LogUtil.e(bytesToHexString(temp))
            preBytes = temp.copyOfRange(7, temp.size-1)
            temp = byteArrayOf()
            RtWave(preBytes).wFs
        }.flatMapConcat {
            it.asFlow()
        }
        .onStart { startTime = TimeSource.Monotonic.markNow() }
        .scan(emptyList<Sample>()) { accumulator, value ->
            val t = startTime!!.elapsedNow().inWholeMilliseconds / 5000f
            accumulator.takeLast(200) + Sample(t, value.toFloat())
        }
        .filter { it.size > 3 }
        .catch {
            LogUtil.e("data error $it")
        }


    private fun getRtWave(): Flow<String> = flow {
        while (true) {
//            LogUtil.e("获取波形")
            val byteArray = byteArrayOf(
                0xAA.toByte(),
                0x1B,
                0xE4.toByte(),
                0x00,
                0x00,
                0x01,
                0x00,
                0x00,
                0x5E
            )
            bleDevice.write(byteArray)
            emit("获取波形: ${bytesToHexString(byteArray)}")
            delay(1_00L)
        }
    }


    override fun onCleared() {
        peripheralScope.launch {
            viewModelScope.coroutineContext.job.join()
            peripheral.disconnect()
            scope.cancel()
        }
    }
}


fun bytesToHexString(src: ByteArray): String {
    val stringBuilder = StringBuilder("")
    for (i in src.indices) {
        val v: Int = src[i].toInt() and 0xFF
        val hv = Integer.toHexString(v)
        if (hv.length < 2) {
            stringBuilder.append(0)
        }
        stringBuilder.append(hv)
        stringBuilder.append(", ")
    }
    return stringBuilder.toString()
}
