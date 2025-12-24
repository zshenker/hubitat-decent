/*
 * Decent DE1 Hubitat Integration
 *
 * Requires decent-advanced-rest-api plugin
 * https://github.com/randomcoffeesnob/decent-advanced-rest-api
 */
metadata {
    definition(name: "Decent DE1", namespace: "community", author: "Community", importUrl: "https://raw.githubusercontent.com/zshenker/hubitat-decent/refs/heads/main/hubitat-decent.groovy") {
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
    }
}

preferences {
        input "decentAddress", "text", title: "decentAddress", required: true, defaultValue: "10.0.0.51:8888"
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

def updated() {
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
}

def parse(String description) {
    if (logEnable) log.debug(description)
}

def on() {
    sendActiveStatus("true")
}

def off() {
    sendActiveStatus("false")
}

def sendActiveStatus(activeValue) {
    def uri = "http://${decentAddress}/api/status?auth=null"
    if (logEnable) log.debug "Sending POST request to [${uri}] with active: ${activeValue}"

    try {
        def params = [
            uri: uri,
            contentType: "application/json",
            requestContentType: "application/json",
            body: [active: activeValue]
        ]

        httpPost(params) { resp ->
            if (resp.success) {
                def switchValue = (activeValue == "true") ? "on" : "off"
                sendEvent(name: "switch", value: switchValue, isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to update status failed: ${e.message}"
    }
}