package com.lepu.ble

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.characteristicOf
import com.juul.kable.logs.Logging
import com.lepu.ble.utils.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged


val serviceUuid = uuidFrom("14839ac4-7d7e-415c-9a42-167340cf2339")
private val writeUuid = uuidFrom("8b00ace7-eb0b-49b0-bbe9-9aee0a26e1a3")
private val notifyUuid = uuidFrom("0734594a-a8e7-4b1a-a6b1-cd5243059a57")

private val writeCharacteristic =
    characteristicOf(service = serviceUuid, characteristic = writeUuid)

private val notifyCharacteristic =
    characteristicOf(service = serviceUuid, characteristic = notifyUuid)

val scanner = Scanner {
    logging {
        level = Logging.Level.Events
    }
}


class BleDevice(private val peripheral: Peripheral) : Peripheral by peripheral {

    val notify: Flow<ByteArray> = peripheral.observe(notifyCharacteristic).distinctUntilChanged()

    suspend fun write(data: ByteArray) {
//        LogUtil.e(bytesToHexString(data))
        peripheral.write(writeCharacteristic, data, WithoutResponse)
    }
}


private fun characteristicOf(service: Uuid, characteristic: Uuid) =
    characteristicOf(service.toString(), characteristic.toString())
