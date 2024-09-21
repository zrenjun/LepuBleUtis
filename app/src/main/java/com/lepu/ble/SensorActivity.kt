package com.lepu.ble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juul.exercise.annotations.Exercise
import com.juul.exercise.annotations.Extra
import com.juul.krayon.element.view.ElementView
import com.juul.krayon.element.view.ElementViewAdapter
import com.lepu.ble.ViewState.Disconnected
import com.lepu.ble.ui.theme.LepuBleUtisTheme


@Exercise(Extra("macAddress", String::class))
class SensorActivity : ComponentActivity() {

    @Suppress("UNCHECKED_CAST")
    private val viewModel by viewModels<SensorViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = SensorViewModel(application, extras.macAddress) as T
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LepuBleUtisTheme {
                Column(
                    Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                ) {
                    TopAppBar(title = { Text("Ble Example") })

                    ProvideTextStyle(
                        TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.background))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            val viewState = viewModel.viewState.collectAsState(Disconnected).value
                            Text(viewState.label, fontSize = 18.sp)
                            Spacer(Modifier.size(10.dp))
                            Text("接收:", fontSize = 18.sp)
                            Spacer(Modifier.size(10.dp))
                            val wave = viewModel.wave.collectAsState(byteArrayOf()).value
                            Text(bytesToHexString(wave), fontSize = 18.sp)
                            Spacer(Modifier.size(10.dp))
                            if (wave.isNotEmpty()) {
                                Text(RtWave(wave).toString(), fontSize = 18.sp)
                                Spacer(Modifier.size(20.dp))
                            }

                            AndroidView(
                                modifier = Modifier.weight(1f),
                                factory = { context ->
                                    ElementView(context).apply {
                                        adapter = ElementViewAdapter(
                                            dataSource = viewModel.data,
                                            updater = ::chart,
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



