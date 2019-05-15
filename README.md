# Profildienst

* Webanwendung, die Neuerscheinungen aus diversen Quellen einliest und Nutzern zur Bearbeitung zuweist.
* Jedem Nutzer sind Sachgruppen zugeordnet über die die Zuordnung stattfindet.
* Die Nutzer können die Titel dann zum Kauf vormerken, ablehnen oder einem anderen Nutzer zuweisen.

### Konfiguration

* Konfigurationsverzeichnis einrichten mit
    * _logback.xml_ - Logging-Konfiguration
    * _profileservice.properties_ - Konfiguration der Anwendung
        * Einrichten der angebundenen Systeme: [s.u.](#angebundene-systeme)
        * Einrichten des Bestandsabgleiches: [s.u.](#bestandsabgleich)
        * Datenbank

                spring.datasource.url=jdbc:mariadb://localhost:3306/profildienst
                spring.datasource.driverClassName=org.mariadb.jdbc.Driver
                spring.datasource.username=user
                spring.datasource.password=pw
                spring.datasource.testWhileIdle=true
                spring.datasource.validationQuery=SELECT 1

    * siehe Beispiel unter _envConf/sample_

* Konfigurationsverzeichnis setzen mit _envConfigDir=PATH_
    
    * zB via _context.xml.default_ im Tomcat
            
            <Context>
            	<!-- path to the config-directory that contains all config-files of the application -->
            	<Environment type="java.lang.String" name="envConfigDir" value="/some/path/conf/" override="false"/>
            </Context>

    * oder zB via Command-Ling-Argument
    
            mvn spring-boot:run -Dspring-boot.run.arguments=--envConfigDir=/soma/path/conf
     
* Erste Anmeldung möglich mit User _admin/admin_, dann können selbst User angelegt werden und der Admin gelöscht werden.


### Angebundene Systeme

Der Import findet regelmäßig statt über den _eu.tib.profileservice.scheduling.DocumentImportJob_ wenn ein Cron-Schedule für das jeweilige System gesetzt ist.

##### DNB

* Neuerscheinungen + Reihe A + Reihe B werden importiert via OAI (siehe <https://www.dnb.de/DE/Service/DigitaleDienste/OAI/oai_node.html>) im Format _marcxml_

        https://services.dnb.de/oai/accessToken~${token}/repository?verb=ListRecords&from=2019-02-18&until=2019-02-18&set=dnb-all:reiheN&metadataPrefix=MARC21-xml

* Es werden nur Titel importiert mit passender _Bibliography Number_. Dabei muss sowohl Jahr als auch Woche zum aktuellen Import-Datumsbereich passen. Muster _YY_,[ABN]_WW_
* Konfiguration in den _profileservice.properties_
     * externalsystem.dnb.baseurl - URL der OAI-Schnittstelle
     * externalsystem.dnb.token - DNB access token
     * externalsystem.dnb.schedule.cron - Cron Schedule des Import-Jobs;  wenn leer, dann gibt es keinen automatischen Import für diesen Connector
     * Beispiel  

             externalsystem.bl.baseurl=http://www.bl.uk/bibliographic/bnbrdf
             externalsystem.bl.schedule.cron=0 0 3 ? * THU

##### LOC
* TODO

##### BL
* Neuerscheinungen der letzten 25 Wochen verfügbar im Format RDF/XML als Zip-Datei via http: <http://www.bl.uk/bibliographic/bnbrdfxml.html>, zB <http://www.bl.uk/bibliographic/bnbrdf/bnbrdf_N3536.zip>
* Die Zip-Datei hat eine fortlaufende Nummer, die jede Woche erhöht wird. Allerdings gibt es auch Wochen, die ausgelassen werden (zB auf Grund von Feiertagen).
    * => Das macht es schwierig zu einem Datum die aktuelle Nummer und damit Datei zu ermitteln.
    * aktueller Ansatz: Dateinummer zu einem Datum aus der Übersicht (html, s.o.) ermitteln: html parsen
* Konfiguration in den _profileservice.properties_
     * externalsystem.bl.baseurl - base URL für die rdf Dateien (http://www.bl.uk/bibliographic/bnbrdf)
     * externalsystem.bl.schedule.cron - Cron Schedule des Import-Jobs;  wenn leer, dann gibt es keinen automatischen Import für diesen Connector
     * Beispiel  

             externalsystem.dnb.baseurl=https://services.dnb.de/oai
             externalsystem.dnb.token=insert-your-token-here
             externalsystem.dnb.schedule.cron=0 0 3 * * ?

### Kategorien (Sachgruppen)

* Über die Sachgruppen wird ermittelt wem eine Neuerscheingung zugeordnet werden soll.
     * Jedem Nutzer ist eine Menge von Sachgruppen zugeordnet.
     * Eine Sachgruppe kann nicht mehrfach verteilt werden. Wird eine Sachgruppe einem Nutzer zugeordnet, obwohl sie bereits einem anderen Nutzer zugeordnet war, so wird die Zuordnung beim alten Nutzer entfernt.
* Hat eine Neuerscheinung mehrere Sachgruppen, die verschiedenen Nutzern zugeordnet sind, so findet die Zuordnung zum passenden Nutzer zufällig statt.

Vorhandene Sachgruppen
 * DDC (DNB+BL)
    
### Bestandsabgleich

* Titel können beim Import gegen den eigenen Bestand abgeglichen werden (optional).
* In der Darstellung der Titel erhält man dann die Information, ob der Titel vorhanden ist oder nicht.
* Als "vorhanden" wird der Titel markiert wenn eine(!) ISBN des Titels im Bestand vorhanden ist.
* Berücksichtigt wird dabei ein externer Bestand, der über einen _InventoryConnector_ abgefragt wird.
* Kriterien für den Abgleich im Bestand: ISBN


#### InventoryConnector

Der (remote) _InventoryConnector_ wird aktiviert in den _profileservice.properties_ über _inventory.system_. Der dort konfigurierte Connector wird in der Configuration _WebConfig_ eingerichtet (weitere Implementationen müssen hier auch registriert werden).

Ist dort kein Connector konfiguriert, so findet kein Abgleich zum externen Bestand statt.

Implementierte Connectoren: 

##### TibConnector 
* SRU-Schnittstelle + Prüfung wieviele Ergebnisse geliefert werden.
* Kriterien für den Abgleich: ISBN
* Aktivieren: _profileservice.properties_: _inventory.system=tib_
* Konfiguration _profileservice.properties_
     * _inventory.tib.baseurl_ (URL der SRU-Schnittstelle)
     * Beispiel  

             inventory.system=tib
             inventory.tib.baseurl=https://getinfo.tib.eu/sru


### Filterregeln

Es können Regeln erstellt werden, so dass Titel direkt beim Import abgelehnt werden können. Die Konfiguration der Filterregeln ist über die Weboberfläche möglich.
Es können folgende Bedingungen konfiguriert werden:
* __Formschlagwort + regulärer Ausdruck__: es wird geprüft, ob der reguläre Ausdruck auf ein beliebiges Formschlagwort der Neuerscheinung passt.
    * **Beispiel**: _.*Jugendbuch.*_ => zB wird Neuerscheinung mit Formschlagwort "Jugendbuch ab 11 Jahren" aussortiert.
* __Sachgruppe + regulärer Ausdruck__: es wird geprüft, ob der reguläre Ausdruck auf eine beliebige Sachgruppe der Neuerscheinung passt.
    * **Beispiel**: _123.*_ => zB wird Neuerscheinung mit Sachgruppe 123.456 aussortiert.
    
### Cleanup

* Die Daten werden nach einem bestimmten Zeitraum wieder aus dem System gelöscht über den _eu.tib.profileservice.scheduling.DocumentCleanupJob_.
* Dazu hat jedes Dokument ein "expiryDate", das angibt wann das Dokument gelöscht werden soll. Wird dieses Datum erreicht, dann wird das Dokument bei der nächsten Ausführung des Jobs gelöscht.
* In der Property _document.expiry.months.default_ wird konfiguriert wieviele Monate nach dem Import das Dokument gelöscht wird.

### Zurückstellen

Kann nicht sofort entschieden werden was mit einem Titel geschehen soll, dann kann der Titel zurückgestellt werden. Der Titel erhält den Status "zurückgestellt" und das Verfallsdatum (an dem der Titel gelöscht wird) kann angepasst werden.

### Priorisierung der Quellen

* Es gibt die Möglichkeit den verschiedenen Quellen eine Priorisierung zuzuordnen.
* Wenn ein Titel in verschiedenen Quellen vorhanden ist, dann werden die Daten aus der höher priorisierten Quelle verwendet. Sind die Daten bereits aus der niedriger priorisierten Quelle importiert, dann werden diese Daten überschrieben.
* Wenn der neue Datensatz mehreren alten Datensätzen zugeordnet werden kann, dann gibt es kein Update der alten Daten. Bei der Surjektion könnten sonst Informationen verloren gehen (wenn die Datensätze bereits bearbeitet wurden)
* Die Priorisierung wird angegeben via Property _document.source.priorities_ - dort werden die Quellen kommagetrennt ausgeführt. Die am weitesten vorne stehende Quelle hat die höchste Priorität.

### Export

* Die akzeptierten Dokumente können exportiert werden um sie an den Erwerb weiterzuleiten.
* Die exportierten Dokumente werden zunächst in eine temporäre Datei geschrieben und zum Download angeboten.
    * Der nächtliche Cleanup-Job löscht die Dateien wieder.
* Exportierte Dokumente erhalten einen neuen Status, damit sie nur einmalig exportiert werden.
    * Dokumente, die gerade exportiert werden, erhalten den Status EXPORTING.
    * Dokumente, die bereits exportiert wurden, erhalten den Status EXPORTED.
    
### Datenbankupdates

* Die Initialisierung des DB-Schemas und nachfolgende Updates werden über liquibase beim Deployment der war automatisch eingepflegt.
* <https://www.liquibase.org>
* Die Liquibase Change-Sets können über das Liquibase-Maven-Plugin generiert werden. Dazu die Konfiguration des Plugins anpassen (aktuelle DB konfigurieren) und mit 'mvn clean compile' + 'mvn liquibase:diff' ausführen. Die Change-Sets sind dann unter _target_ zu finden.