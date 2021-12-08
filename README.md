# Laalys
Laalys (Learner Activity AnaLYser) is a software enabling to analyze player's traces in order to generate pedagogical labels about the learner's behavior in Serious Games. The first version of Laalys was designed during doctoral works of [Pradeepa Thomas](https://www.lip6.fr/actualite/personnes-fiche.php?ident=D1069).

Laalys is included inside [E-LearningScape](https://github.com/Mocahteam/E-LearningScape) to produce in-game feedbacks depending on players' difficulties.

# Download
Download Laalys here : https://github.com/Mocahteam/Laalys/releases/download/V1/Laalys.zip

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
