# Laalys
Laalys (Learner Activity AnaLYser) is a software enabling to analyze player's traces in order to generate pedagogical labels about the learner's behavior in Serious Games. The first version of Laalys was designed during doctoral works of [Pradeepa Thomas](https://www.lip6.fr/actualite/personnes-fiche.php?ident=D1069).

Laalys is included inside [E-LearningScape](https://github.com/Mocahteam/E-LearningScape) to produce in-game feedbacks depending on players' difficulties.

# Download
Download Laalys here : [https://github.com/Mocahteam/Laalys/releases/download/V2.2/LaalysV2.2.zip](https://github.com/Mocahteam/Laalys/releases/download/V2.2/LaalysV2.2.zip)

# Requirements
Java 1.8

# How it works
Laalys use Petri nets to label player actions. The release archive contains the application (.jar files) and some examples of Petri nets, specifications and traces:
- Launch LaalysV2.jar
- In the first tab (named "Petri nets selection"):
  - Select fullPetriNets folder
  - Select filteredPetriNets folder (leave options to default)
  - Select specification folder
  - Click button named "Load Petri nets and specifications" (this enabled the second tab)
- In the second tab (named "Traces management"):
  - You can build a fictive trace or load an example trace with the "Load traces from files" button (this enabled the third tab)
- In the third tab (named "Analysis"):
  - Click on "Launch analysis" button

# Using Laalys by script
This is the Python Script example included into the release
```Python
# You can use LaalysV2.jar with UI by executing it directly

# You can also communicate with LaalysV2.jar with sockets
# This script is an example to show you how to communicate with Laalys with sockets

import socket
import subprocess

# Send a request to Laalys and return result
def sendRequest(keyword, options):
        request = keyword
        for opt in options:
                request = request + "\t" + opt
        print ("Send request: "+request)
        client.send(request.encode())
        print ("Waiting answer...")
        msg = client.recv(2048)
        response = msg.decode()
        while len(msg) == 2048:
                msg = client.recv(2048)
                response = response + msg.decode()
        return response[:-2] #remove the last \r\n

print ("Launch server")
socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket.bind(('127.0.0.1', 12012))
print ("Launch Laalys")
fullPetriNetsPath = "./fullPetriNets" # contains Petri nets with all actions available
filteredPetriNetsPath = "./filteredPetriNets" # contains Petri nets with actions selected by experts
featuresPath = "./specifications" # define for each action if it is a player/system action and if it is an end action
graphType = "ACCESS" # If ACCESS compute Accessible graph, if COVER compute coverability graph
# Launch Laalys jar file
subprocess.Popen(["java.exe", "-jar", "./LaalysV2.jar", "-fullPn", fullPetriNetsPath, "-filteredPn", filteredPetriNetsPath, "-features", featuresPath, "-serverIP", "localhost", "-serverPort", "12012", "-kind", graphType])
print ("Waiting Laalys connexion")
socket.listen()
client, address = socket.accept()
print ("Laalys connected")
print ("")

# Get current markings of Petri nets
print ("Step 1: Get current marking for all Petri nets from Laalys")
initialMarkings = sendRequest("GetPetriNetsMarkings", [])
print (initialMarkings)
print ("")

# Get the minimal path to reach a given transition
rdpName = "frozenDoor"
targetAction = "open door - exit" # see ./fullPetriNets/frozenDoor.pnml, open it with Tina tool (https://projects.laas.fr/tina/)
nbActionMax = "10"
print ("Step 2: Ask Laalys to give the minimal path to reach the \""+targetAction+"\" transition in the \""+rdpName+"\" Petri net.")
response = sendRequest("NextActionToReach", [rdpName, targetAction, nbActionMax])
print ("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))
print ("")

# Perform a specific action
actionName = "grab key"
performedBy = "player" # Has to be "player" or "system" accordingly with ./specification/frozenDoor.xml
print ("Step 3: Ask Laalys to perform the \""+actionName+"\" action in the \""+rdpName+"\" Petri net.")
response = sendRequest(rdpName, [actionName, performedBy])
print ("Response received: "+response)
print ("")

# Get the minimal path to reach a given transition
print ("Step 4: Ask Laalys to give the minimal path to reach the \""+targetAction+"\" transition in the \""+rdpName+"\" Petri net.")
response = sendRequest("NextActionToReach", [rdpName, targetAction, nbActionMax])
print ("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))
print ("We can see that the path is shorter than the previous call in step 2.")
print ("")

# Get current markings of targeted Petri net
print ("Step 5: Get current markings of the \""+rdpName+"\" Petri net from Laalys")
savedMarking = sendRequest("GetPetriNetsMarkings", [rdpName])
print (savedMarking)
print ("")

# Get actions available in all Petri nets
print ("Step 6: Ask Laalys all actions currently available in all Petri nets")
response = sendRequest("TriggerableActions", [])
print ("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))
print ("")

# Get actions available in a specific Petri net
print ("Step 7: Ask Laalys all actions currently available in the \""+rdpName+"\" Petri net")
response = sendRequest("TriggerableActions", [rdpName])
print ("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))
print ("")

# Set specific markings
print ("Step 8: Ask Laalys to restore initial markings in all Petri nets")
response = sendRequest("SetPetriNetsMarkings", [initialMarkings])
print ("Response received: "+response)
print ("")

# Get the minimal path to reach a given transition
print ("Step 9: Ask Laalys to give the minimal path to reach the \""+targetAction+"\" transition in the \""+rdpName+"\" Petri net.")
response = sendRequest("NextActionToReach", [rdpName, targetAction, nbActionMax])
print ("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))
print ("We find the same result as step 2, it's because we restaured markings in initial state in step 8.")
print ("")

# Set specific markings
print ("Step 10: Ask Laalys to restore markings in the \""+rdpName+"\" Petri net")
response = sendRequest("SetPetriNetsMarkings", [savedMarking])
print ("Response received: "+response)
print ("")

# Get actions available in a specific Petri net
print ("Step 11: Ask Laalys all actions currently available in the \""+rdpName+"\" Petri net")
response = sendRequest("TriggerableActions", [rdpName])
print ("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))
print ("We find the same result as step 7, it's because we restaured the markings saved in step 5.")
print ("")

# Refresh Petri nets from its current state
print ("Step 12: Ask Laalys to refresh \""+rdpName+"\" Peti nets accessible/coverability graph by using current markings as initial markings")
response = sendRequest("ResetPetriNetsFromCurrentMarkings", [rdpName])
print ("Response received: "+response)
print ("")

# Close connection with Laalys
print ("Close")
client.send("Quit".encode())
client.close()
socket.close()
```
Execution of this script gives the following result
```
Launch server
Launch Laalys
Waiting Laalys connexion
Laalys connected

Step 1: Get current marking for all Petri nets from Laalys
Send request: GetPetriNetsMarkings
Waiting answer...
frozenDoor	1:0:1:0:0:1	1:0:1:0:0:1		Thermometer	6:0:0:1:0:1:0:2:0:2:0:2:0:1:1:1	6:0:0:1:0:1:0:2:0:2:0:2:0:1:1:1		murDeGlace	0:0:1:0:1:0:0:1:0:1:0:0:0:6:0:0:1:0:0:1:1:0:6:1:1:0:1:0:0:1:1:1	0:0:1:0:1:0:0:1:0:1:0:0:0:6:0:0:6:1:0:0:1:1

Step 2: Ask Laalys to give the minimal path to reach the "open door - exit" transition in the "frozenDoor" Petri net.
Send request: NextActionToReach	frozenDoor	open door - exit	10
Waiting answer...
Response received: grab key	turn on boiler	open door - exit
Nb actions: 3

Step 3: Ask Laalys to perform the "grab key" action in the "frozenDoor" Petri net.
Send request: frozenDoor	grab key	player
Waiting answer...
Response received: correct

Step 4: Ask Laalys to give the minimal path to reach the "open door - exit" transition in the "frozenDoor" Petri net.
Send request: NextActionToReach	frozenDoor	open door - exit	10
Waiting answer...
Response received: turn on boiler	open door - exit
Nb actions: 2
We can see that the path is shorter than the previous call in step 2.

Step 5: Get current markings of the "frozenDoor" Petri net from Laalys
Send request: GetPetriNetsMarkings	frozenDoor
Waiting answer...
frozenDoor	1:0:1:0:1:0	1:0:1:0:1:0

Step 6: Ask Laalys all actions currently available in all Petri nets
Send request: TriggerableActions
Waiting answer...
Response received: turn on boiler	activate-levier_plus_3_1;4	activate-levier_plus_4_1;7	desactivate-levier_plus_6_1;2	activate-levier_plus_2_1;6	activate-levier_plus_2_1;9	store-clef;5
Nb actions: 7

Step 7: Ask Laalys all actions currently available in the "frozenDoor" Petri net
Send request: TriggerableActions	frozenDoor
Waiting answer...
Response received: turn on boiler
Nb actions: 1

Step 8: Ask Laalys to restore initial markings in all Petri nets
Send request: SetPetriNetsMarkings	frozenDoor	1:0:1:0:0:1	1:0:1:0:0:1		Thermometer	6:0:0:1:0:1:0:2:0:2:0:2:0:1:1:1	6:0:0:1:0:1:0:2:0:2:0:2:0:1:1:1		murDeGlace	0:0:1:0:1:0:0:1:0:1:0:0:0:6:0:0:1:0:0:1:1:0:6:1:1:0:1:0:0:1:1:1	0:0:1:0:1:0:0:1:0:1:0:0:0:6:0:0:6:1:0:0:1:1
Waiting answer...
Response received: frozenDoor done!	Thermometer done!	murDeGlace done!

Step 9: Ask Laalys to give the minimal path to reach the "open door - exit" transition in the "frozenDoor" Petri net.
Send request: NextActionToReach	frozenDoor	open door - exit	10
Waiting answer...
Response received: grab key	turn on boiler	open door - exit
Nb actions: 3
We find the same result as step 2, it's because we restaured markings in initial state in step 8.

Step 10: Ask Laalys to restore markings in the "frozenDoor" Petri net
Send request: SetPetriNetsMarkings	frozenDoor	1:0:1:0:1:0	1:0:1:0:1:0
Waiting answer...
Response received: frozenDoor done!

Step 11: Ask Laalys all actions currently available in the "frozenDoor" Petri net
Send request: TriggerableActions	frozenDoor
Waiting answer...
Response received: turn on boiler
Nb actions: 1
We find the same result as step 7, it's because we restaured the markings saved in step 5.

Step 12: Ask Laalys to refresh "frozenDoor" Peti nets accessible/coverability graph by using current markings as initial markings
Send request: ResetPetriNetsFromCurrentMarkings	frozenDoor
Waiting answer...
Response received: frozenDoor done!

Close
```
