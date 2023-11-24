# Laalys
Laalys (Learner Activity AnaLYser) is a software enabling to analyze player's traces in order to generate pedagogical labels about the learner's behavior in Serious Games. The first version of Laalys was designed during doctoral works of [Pradeepa Thomas](https://www.lip6.fr/actualite/personnes-fiche.php?ident=D1069).

Laalys is included inside [E-LearningScape](https://github.com/Mocahteam/E-LearningScape) to produce in-game feedbacks depending on players' difficulties.

# Download
Download Laalys here : [https://github.com/Mocahteam/Laalys/releases/download/V1.1/Laalys.zip](https://github.com/Mocahteam/Laalys/releases/download/V1.1/LaalysV1.1.zip)

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
This is the Python Script example included into release
```
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
        print("Send request: "+request)
        client.send(request.encode())
        print("Waiting answer...")
        msg = client.recv(2048)
        response = msg.decode()
        while len(msg) == 2048:
                msg = client.recv(2048)
                response = response + msg.decode()
        return response[:-2] #remove the last \r\n

print("Launch server")
socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket.bind(('127.0.0.1', 12012))
print("Launch Laalys")
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

# Get initial markings of Petri nets from Laalys
initialMarkings = sendRequest("GetPetriNetsMarkings", [])
print (initialMarkings)

# Ask Laalys to give the minimal path te reach a give transition
rdpName = "frozenDoor"
targetAction = "open door - exit" # see ./fullPetriNets/frozenDoor.ndr
nbActionMax = "10"
response = sendRequest("NextActionToReach", [rdpName, targetAction, nbActionMax])
print("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))

# Ask Laalys to perform an action
actionName = "grab key"
performedBy = "player" # Has to be "player" or "system" accordingly with ./specification/frozenDoor.xml
response = sendRequest(rdpName, [actionName, performedBy])
print("Response received: "+response)

# The path to reach the target transition is now shorter
response = sendRequest("NextActionToReach", [rdpName, targetAction, nbActionMax])
print("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))

# Ask Laalys all actions currently available
response = sendRequest("TriggerableActions", [])
print("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))

# Ask Laalys to restore specific marking (here the initial marking)
response = sendRequest("SetPetriNetsMarkings", [initialMarkings])
print("Response received: "+response)

# Same result as initial situation
response = sendRequest("NextActionToReach", [rdpName, targetAction, nbActionMax])
print("Response received: "+response)
print ("Nb actions: "+str(len(response.split('\t'))))

print ("Close")
client.close()
socket.close()
```
