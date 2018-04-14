package com.joaomariodev.rmsfsensoractuationapp.Model

//DEVICE FORMAT
class TTNDevice (var deviceID:String){
        var temperature_status: String = ""
        var temperature_threshold : String = ""
        var gas_status: String = ""
        var gas_threshold: String = ""
        var water_status: String = ""
        var water_operational: String = ""
        var alarm_status: String = ""
        var alarm_operational: String = ""
        var data_initialized: Boolean = false
}
