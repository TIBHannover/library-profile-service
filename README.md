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

                spring.datasource.url=url
                spring.datasource.driverClassName=driver
                spring.datasource.username=user
                spring.datasource.password=pw

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

* Neuerscheinungen werden importiert via OAI (siehe <https://www.dnb.de/DE/Service/DigitaleDienste/OAI/oai_node.html>) im Format _marcxml_

        https://services.dnb.de/oai/accessToken~${token}/repository?verb=ListRecords&from=2019-02-18&until=2019-02-18&set=dnb-all:reiheN&metadataPrefix=MARC21-xml

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
    * Ansätze: TODO: welchen? andere?
         * Dateinummer errechnen, bekannte Ausnahmen berücksichtigen: Ausnahmen müssen ständig aktualisiert werden :(
         * Dateinummer zu einem Datum aus der Übersicht (html, s.o.) ermitteln: html parsen und hoffen, dass sich die html-Seite nicht ändert :(
         * Letzte Dateinummer errechnen und schauen, ob die Datei verfügbar ist. Falls nicht: Nummer runterzählen bis Datei gefunden wird. Raten, ungenau, mit der Zeit immer mehr unnötige Abfragen :(
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
     * Eine Sachgruppe kann nicht mehrfach verteilt werden.
* Hat eine Neuerscheinung mehrere Sachgruppen, die verschiedenen Nutzern zugeordnet sind, so findet die Zuordnung zum passenden Nutzer zufällig statt.

Vorhandene Sachgruppen
 * DDC (DNB+BL)
    
### Bestandsabgleich

Titel werden gegen den eigenen Bestand abgeglichen, so dass Titel, die im eigenen Bestand vorhanden sind nicht importiert werden. Berücksichtigt wird dabei der lokale Bestand der Anwendung und ein(!) externer Bestand (optional), der über einen _InventoryConnector_ abgefragt wird.

Kriterien für den Abgleich im lokalen Bestand: ISBN

#### InventoryConnector

Der (remote) _InventoryConnector_ wird aktiviert in den _profileservice.properties_ über _inventory.system_. Der dort konfigurierte Connector wird in der Configuration _WebConfig_ eingerichtet (weitere Implementationen müssen hier auch registriert werden).

Ist dort kein Connector konfiguriert, so findet kein Abgleich zum externen Bestand statt.

Implementierte Connectoren: 

##### TibConnector 
* SRU-Schnittstelle + Prüfung wieviele Ergebnisse geliefert werden.
* Kriterien für den Abgleich: ISBN
     * **TODO**:
     
> zur Klärung und Ergänzung des Punktes Dublettencheck gegen unseren OPAC noch ein Nachtrag:
>
> E-Books enthalten oftmals zwei ISBNs, nämlich die des E-Books selbst und die der Printausgabe (Parallelausgabe).
> Wenn im OPAC daher ein Titel nur als E-Book vorhanden ist (dies betrifft v.a. eingespielte große E-Book-Pakete,  z.B. Springer oder EBA), dann darf die Printausgabe aus dem Profildienst nicht entfernt werden.
     
* Aktivieren: _profileservice.properties_: _inventory.system=tib_
* Konfiguration _profileservice.properties_
     * _inventory.tib.baseurl_ (URL der SRU-Schnittstelle)
     * _inventory.tib.recordnrpath_ (XPath Pfad zur Anzahl der Ergebnisse im Reply)
     * Beispiel  

             inventory.system=tib
             inventory.tib.baseurl=https://getinfo.tib.eu/sru
             inventory.tib.recordnrpath=/searchRetrieveResponse/numberOfRecords


### Filterregeln

Es können Regeln erstellt werden, so dass Titel direkt beim Import abgelehnt werden können. Die Konfiguration der Filterregeln ist über die Weboberfläche möglich.
Es können folgende Bedingungen konfiguriert werden:
* __Formschlagwort + regulärer Ausdruck__: es wird geprüft, der reguläre Ausdruck auf ein beliebiges Formschlagwort der Neuerscheinung passt.
    * **Beispiel**: _.*Jugendbuch.*_ => zB wird Neuerscheinung mit Formschlagwort "Jugendbuch ab 11 Jahren" aussortiert.
* __Sachgruppe + regulärer Ausdruck__: es wird geprüft, der reguläre Ausdruck auf eine beliebige Sachgruppe der Neuerscheinung passt.
    * **Beispiel**: _123.*_ => zB wird Neuerscheinung mit Sachgruppe 123.456 aussortiert.
    
### Cleanup

Die Daten werden nach einem bestimmten Zeitraum wieder aus dem System gelöscht über den _eu.tib.profileservice.scheduling.DocumentCleanupJob_.

**TODO**: dabei keine Titel mit Status "zurückgestellt" löschen oder mit "expiryDate" oä arbeiten (noch zu klären).