# Profildienst

* Webanwendung, die Neuerscheinungen aus diversen Quellen einliest und Nutzern zur Bearbeitung zuweist.
* Jedem Nutzer sind Sachgruppen zugeordnet über die die Zuordnung stattfindet.
* Die Nutzer können die Titel dann zum Kauf vormerken, ablehnen oder einem anderen Nutzer zuweisen.

### Konfiguration
* Einrichten der angebundenen Systeme: _s.u._
* Einrichten des Bestandsabgleiches: _s.u._
* Erste Anmeldung möglich mit User _admin/admin_

### Angebundene Systeme

Der Import findet regelmäßig statt über den _eu.tib.profileservice.scheduling.DocumentImportJob_.

##### DNB

* Neuerscheinungen werden importiert via OAI (siehe <https://www.dnb.de/DE/Service/DigitaleDienste/OAI/oai_node.html>) im Format _marcxml_

```
https://services.dnb.de/oai/accessToken~${token}/repository?verb=ListRecords&from=2019-02-18&until=2019-02-18&set=dnb-all:reiheN&metadataPrefix=MARC21-xml
```
* Konfiguration in den _application.properties_
     * externalsystem.dnb.baseurl - URL der OAI-Schnittstelle
     * externalsystem.dnb.token - DNB access token

##### LOC
* TODO

##### BL
* TODO

### Kategorien (Sachgruppen)

* Über die Sachgruppen wird ermittelt wem eine Neuerscheingung zugeordnet werden soll.
     * Jedem Nutzer ist eine Menge von Sachgruppen zugeordnet.
     * Eine Sachgruppe kann nicht mehrfach verteilt werden.
* Hat eine Neuerscheinung mehrere Sachgruppen, die verschiedenen Nutzern zugeordnet sind, so findet die Zuordnung zum passenden Nutzer zufällig statt.

Vorhandene Sachgruppen
 * DDC (DNB)
    
### Bestandsabgleich

Titel werden gegen den eigenen Bestand abgeglichen werden, so dass Titel, die im eigenen Bestand vorhanden sind nicht importiert werden. Berücksichtigt wird dabei der lokale Bestand der Anwendung und ein externen Bestand, der über einen _InventoryConnector_ abgefragt wird.

Implementiert: _TibConnector_ - SRU-Schnittstelle + Prüfung wieviele Ergebnisse geliefert werden.

Konfiguration über _inventory.tib.baseurl_ (URL der SRU-Schnittstelle) und _inventory.tib.recordnrpath_ (XPath Pfad zur Anzahl der Ergebnisse im Reply) in den _application.properties_.

### Filterregeln

Es können Regeln erstellt werden, so dass Titel direkt beim Import abgelehnt werden können. Die Konfiguration der Filterregeln ist über die Weboberfläche möglich.
Es können folgende Bedingungen konfiguriert werden:
* __Formschlagwort + regulärer Ausdruck__: es wird geprüft, der reguläre Ausdruck auf ein beliebiges Formschlagwort der Neuerscheinung passt.
    * **Beispiel**: _.*Jugendbuch.*_ => zB wird Neuerscheinung mit Formschlagwort "Jugendbuch ab 11 Jahren" aussortiert.
* __Sachgruppe + regulärer Ausdruck__: es wird geprüft, der reguläre Ausdruck auf eine beliebige Sachgruppe der Neuerscheinung passt.
    * **Beispiel**: _123.*_ => zB wird Neuerscheinung mit Sachgruppe 123.456 aussortiert.
    
### Cleanup

Die Daten werden nach einem bestimmten Zeitraum wieder aus dem System gelöscht über den _eu.tib.profileservice.scheduling.DocumentCleanupJob_.

**TODO**: dabei keine Titel mit Status "zurückgestellt" löschen.