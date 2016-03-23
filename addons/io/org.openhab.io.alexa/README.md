

#openHAB Amazon Alexa Skills Binding

The  Alexa Skills Binding exposes openHAB items to the Amazon Alexa voice cloud.

##Features:
* Support most command types
* Auto generates configuration to help with setup

##Configuration:
This binding requires a AWS developer account to use.  You must also expose port 443 to your OH instance, I use ngix right now as a reverse proxy for just /alexa/api .  The binding will verify Amazon Alexa signed requests and rejects others.    

* Install binding
* Tag items you want to expose with the homekit prefix (see #Device Tagging)
* create a new "Alex Skills App"  at https://developer.amazon.com/edw/home.html#/ 
* Give it a Name, wake word (Jarvis, house, Jeeves....) and some version (1.0)
* Go to the configuration page of the binding (/alexa on you openHAB instance)
* enter the full path to your OH exposed instance, like https://myhost.com/alexa/api . Note that this will not work on any other port then 443 (Alexa Skills requirement) .
* Go to "Interaction Model"
* Create two custom slots called "COMMAND" and "LABEL", copy the values from you the binding configuration page to these slots.  If you add more items later to be controlled, you will need to add them back to the "LABEL" slot.
* Copy the "Sample Utterances" from the local config page
* Copy the "Intent Schema" from the local config page
* Save, cross fingers.
* Go to "SSL Certificate" menu
* Copy the Public key for your instance.  If using ngix, this is in your SSL folder (/etc/ngix/ssl), if you are expsoing OH by itself then you can copy the SSL key from the local config page (note I have not tried this, but it should work).  Amazon will not connect to the service unless it has its public key first. Save, cross fingers.
* CLick on "Test" menu and try it out.  For example one might want to say "Alex, ask Jarvis to turn kicthen lights on", so type in the test box "to turn kicthen lights on" and see what happens.

##Device Tagging
To expose an item on the service apply any Apple HomeKit style tag to it.  The item label will be used as the Device name.
```
Switch  TestSwitch1     "Kitchen Switch" ["homekit:Switch"]
Switch  TestSwitch2     "Bathroom" ["homekit:Lightbulb"]
Dimmer  TestDimmer3     "Hallway" ["homekit:DimmableLightbulb"]
Number  TestNumber4     "Cool Set Point" ["homekit:coolingThreshold"]
```