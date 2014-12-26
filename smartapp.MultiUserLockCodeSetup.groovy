/**
 *  MultiUserLockCodeSetup
 *
 *  Copyright 2014 Yves Racine
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
	name: "MultiUserLockCodeSetup",
	namespace: "yracine",
	author: "Yves Racine",
	description: "MultiUserLockCodeSetup",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



preferences {

	page(name:"controllerSetup")
	page(name: "usersSetup")
	page(name: "Notifications")

}

def controllerSetup() {

	dynamicPage(name: "controllerSetup", uninstall: true, nextPage: usersSetup) {
		section("Which Lock?") {
			input "lock1", title: "Lock", "capability.lock"
		}

		section("Users") {
			input "usersCount", title: "Users count (max users setup=10)?", "number"
		}
	} 
}    



def usersSetup() {

	dynamicPage(name: "usersSetup", title: "Users Setup", nextPage: Notifications) {
/*
		section("Delete User" ) {
			input "deleteUser", title: "Lock User id ", "string", description: "Lock User ", required: false
			input "deleteCode", "text", title: "Code", required: false
		}
*/
    	for (int i = 1; ((i <= settings.usersCount) && (i<= 10)); i++) {
	    	section("User " + i + " Setup") {
				input "userName" + i, title: "User Name", "string", description: "User Name " + i, required: false
				input "user" + i, title: "Lock User id ", "string", description: "Lock User " + i, required: false
				input "code" + i, "text", title: "Code", required: false
			}
            
		}

	}

}

def Notifications() {

	dynamicPage(name: "Notifications", title: "Other Options", install: true) {
		section( "Notifications" ) {
			input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
		section([mobileOnly:true]) {
			label title: "Assign a name for this SmartApp", required: false
		}
	}
}


def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(app, appTouch)
	subscribe(lock1, "usercode", usercodeget)
	changeLockCode()
}

def appTouch(evt) {
	changeLockCode()
}


def usercodeget(evt){
	log.debug "usercodeget> Code for user $user1: $lock1.currentUsercode"
}



private def changeLockCode() {
	def key
    def deleteFlag =1

	for (int i = 1; ((i <= settings.usersCount) && (i<= 10)); i++) {

/*
		def key = "delete$i"
		def deleteFlag = settings[key]
*/        
		key = "user$i"
		Integer user = settings[key]?.toInteger()
        
		key = "code$i"
		String code = settings[key]?.toString()

		key = "userName$i"
		def username = settings[key]
        
   
		log.debug "Current Code for ${username}, Lock User id ${user} : ${code} "
		if ((user!=null) && (code != null)) {
/*
			lock1.setCode(user, code)
*/
			lock1.usercodechange(user, code, deleteFlag)
			String msg = "MultiUserLockCodeSetup>set code for ${username} at lock user id ${user}, deleteFlag=$deleteFlag as requested"
			log.debug(msg)
 		} 
        
        
	}
	if ( sendPushMessage != "No" ) runIn(30,"sendConfirmationMsgLater") 

/*
	if ((deleteUser != null) && (deleteCode !=null)) {
		deleteFlag=0
		Integer user = settings.deleteUser?.toInteger()
		String code = settings.deleteCode?.toString()

		lock1.deleteCode(user)


		lock1.usercodechange(user, code, deleteFlag)


		String msg = "MultiUserLockCodeSetup>delete code for ${username} at lock user id ${user}"
    
	}

*/
}

private def sendConfirmationMsgLater() { 

	for (int i = 1; ((i <= settings.usersCount) && (i<= 10)); i++) {

/*
		def key = "delete$i"
		def deleteFlag = settings[key]
*/        
		def key = "user$i"
		Integer user = settings[key]?.toInteger()
        
		key = "userName$i"
		def username = settings[key]
        
		String msg = "MultiUserLockCodeSetup> set code for ${username} at lock user id ${user} on lock ${lock1} as requested"
		send(msg)
	}
	        
}


private send(msg) {
	if ( sendPushMessage != "No" ) {
		log.debug( "sending push message" )
		sendPush( msg )
	}

	if ( phone ) {
		log.debug( "sending text message" )
		sendSms( phone, msg )
	}
	log.debug msg
}

