/**
 *  SG Control
 *
 *  Copyright 2016 Swamp
 *
 */

definition(
    name: "SG Control",
    namespace: "swamp",
    author: "Swamp",
    description: "Lab test for SG app",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "sg", displayLink: "sg"])

preferences {
	section("Allow Endpoint to Control These Things...") {
		input "switches", "capability.switch", title: "Use which Outlet?", multiple: true
        input "presence", "capability.presenceSensor", title: "Which Arrival Sensor?", multiple: true
	}
    /*
    section("Remote endpoint information...") {
    	input "myuri", "text"
    }
    */
}

mappings {
	path("/switch/:id/:command") {
		action: [
			POST: "doSwitch"
		]
	}  
}

def updated() {}



private void initToken() {
  def token = createAccessToken();
  def url = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}";
  
  log.debug "url: $url"
  log.debug "token: $token"
}

def installed() {
  log.debug "installed SG Control";
  subscribe(presence, "presence", myPresenceHandler);

  //initToken();
}

def myPresenceHandler(evt) {
  if("present" == evt.value) {
  	sendPresence(presence, "arrived");
  } else {
  	sendPresence(presence, "departed");
  }
}

private void sendPresence(sensor, command) {
    def myuri = "http://www.ank.com/cgi-bin/sg.pl";
	log.debug "calling out to $myuri"
	def sname = sensor.name;
    def sloc = location;
	def params = [
    	uri: myuri,
        //path: "sg.pl",
        query: [ 'location': sloc, 'sensor': sname, 'cmd': command ]
    ];
    try {
        httpGet(params) { resp ->
            resp.headers.each {
                log.debug "${it.name} : ${it.value}"
            }
            log.debug "response contentType: ${resp.contentType}"
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}


//switches

void doSwitch() {
	update(switches)
}

def deviceHandler(evt) {}

private void update(devices) {
	log.debug "update, request: params: ${params}, devices: $devices.id"
    
	//def command = request.JSON?.command
    def command = params.command
    log.debug "command $command"

	if (command) 
    {
    	devices.each {
            def cval = it.currentValue('switch');
        	log.debug "switch ${it.id} is $cval"

            if(command == "on")
            {
                if(cval && cval == "off") {
                    log.debug "turned on ${it.id}"
                    it.on();
                }
            }
            else if (command == "off")
            {
                if(cval && cval == "on") {
                    log.debug "turned off ${it.id}"
                    it.off();
                }
            }
        }
	}
}

private show(devices, type) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def attributeName = type == "motionSensor" ? "motion" : type
		def s = device.currentState(attributeName)
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
}


private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}

/*
preferences {
    section("Allow Endpoint to Control These Things...") {
		input "switch", "capability.switch", title: "Which Outlet?", multiple: false
        input "presence", "capability.presence", title: "Which Arrival Sensor?", multiple: false
	}
}

mappings {
    path("/presence/:id/:pstate") {
    	action: [
        	GET: "doPresence"
        ]
    }
}



private void update(device) {
	log.debug "update, request: params: ${params}, devices: $devices.id"
    
	//def command = request.JSON?.command
    def pstate = params.pstate

	if (command) 
    {
        if(pstate == "departed")
        {
            if(device.currentValue('switch') == "on") {
            	device.off();
            }
        }
        if(pstate == "arrived") {
            if(device.currentValue('switch') == "off") {
            	device.on();
            }
        }
	}
}

void doPresence() {
	update(switch);
}


def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}


private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}

// TODO: implement event handlers
*/