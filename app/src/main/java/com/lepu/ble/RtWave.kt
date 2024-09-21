package com.lepu.ble

import com.lepu.ble.utils.ByteUtils.byte2UInt
import com.lepu.ble.utils.toUInt


/**
 *
 *  说明: o2血氧波形解析
 *  zrj 2024/9/20 18:44
 *
 */
class RtWave constructor(var bytes: ByteArray) {
    var spo2: Int
    var pr: Int
    var battery: Int         // 电量（0-100%）
    var batteryState: Int    // 充电状态（0：没有充电 1：充电中 2：充电完成）
    var pi: Int
    var state: Int           // 工作状态（0：导联脱落 1：导联连上 其他：异常）
    var len: Int
    var waveByte: ByteArray
    var wFs: IntArray
    var wByte: ByteArray

    init {
        var index = 0
        spo2 = byte2UInt(bytes[index])
        index++
        pr = toUInt(bytes.copyOfRange(index, index + 2))
        index += 2
        battery = byte2UInt(bytes[index])
        index++
        batteryState = byte2UInt(bytes[index])
        index++
        pi = byte2UInt(bytes[index])
        index++
        state = byte2UInt(bytes[index])
        index++
        index += 3
        len = toUInt(bytes.copyOfRange(index, index + 2))
        index += 2
        val temp = index + len
        if (temp > bytes.size) {
            len = bytes.size - index
        }
        waveByte = bytes.copyOfRange(index, index + len)
        wFs = IntArray(len)
        wByte = ByteArray(len)
        for (i in 0 until len) {
            var temp = byte2UInt(waveByte[i])
            // 脉搏音标记-100，oxyfit是-10
            if (temp == 156 || temp == 246) {
                if (i == 0) {
                    if ((i + 1) < len)
                        temp = byte2UInt(waveByte[i + 1])
                } else if (i == len - 1) {
                    temp = byte2UInt(waveByte[i - 1])
                } else {
                    if ((i + 1) < len)
                        temp = (byte2UInt(waveByte[i - 1]) + byte2UInt(waveByte[i + 1])) / 2
                }
            }

            wFs[i] = temp
            wByte[i] = (100 - temp / 2).toByte()
        }
    }

    override fun toString(): String {
        return """
            RtWave : 
            spo2 = $spo2
            pr = $pr
            battery = $battery
            batteryState = $batteryState
            pi = $pi
            state = $state
            len = $len
        """.trimIndent()
    }

}