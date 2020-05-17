# GreenLake
Implementation eines Data Lake-Auswertungstools im Rahmen der T3_3101 Studienarbeit

## License
This software is licensed under GPL v3 License. See the LICENSE-file for more information.

## Setup für die Entwicklung
### Schritt 1 - Wildfly installieren
Da es sich bei dem Projekt um JavaEE-Module handelt, muss ein Application-Server installiert werden. Hierzu kann WildFly heruntergeladen werden: https://wildfly.org/downloads/

### Schritt 2 - NodeJS installieren
Für das Entwickeln der Anwendung wird NodeJS benötigt. Dies kann hier heruntergeladen werden: https://nodejs.org/de/

### Schritt 3 - Projekt klonen
Es wird empfohlen das Projekt zunächst zu klonen und dann ein Unterverzeichnis als Projekt in z.B. IntelliJ als Maven-Projekt zu öffnen (Über "Import Project" und das Hinzufügen des Framework Support für Maven im Project Setup)

### Schritt 4 - Modul bauen
Projekt mit der Maven Konfiguration "clean install" bauen. Im Verzeichnis "greenlake-ear" findet sich dann unter Target eine .ear-Datei.

### Schritt 5 - Modul in WildFly deployen
Das gewünschte Modul kann dann in WildFly deployt werden, indem die .ear-Datei in das Verzeichnis path/to/wildfly/standalone/deployments/ kopiert wird. WildFly kann dann mit der standalone.bat im Verzeichnis path/to/wildfly/bin/standalone.bat deployt werden. 
!! Achtung! Wildfly startet automatisch auf Port 8080 und kann daher mit localhost:8080 im Browser geöffnet werden. Um dies auf Port 80 umzubiegen, kann in der standalone.xml unter path/to/wildfly/standalone/ der Wert für das http socket binding von 8080 auf 80 geändert werden

## Zusätzliche Abhängigkeiten für Apache Hadoop
Diese Datei ersetzt das Verzeichnis /path/to/hadoop/bin
https://1drv.ms/u/s!AmfFlVyUTCzOo8B1gjGztGaqFrPdVA?e=cESl2L
