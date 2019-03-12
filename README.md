# Profildienst

Webanwendung, die Neuerscheinungen aus diversen Quellen einliest und Nutzern zur Bearbeitung zuweist. Jedem Nutzer sind Sachgruppen zugeordnet über die die Zuordnung stattfindet.
Die Nutzer können die Titel dann zum Kauf vormerken, ablehnen oder einem anderen Nutzer zuweisen.

### Angebundene Systeme

Der Import findet regelmäßig statt über den _eu.tib.profileservice.scheduling.DocumentImportJob_.

* DNB
    * Neuerscheinungen werden importiert via OAI

```
https://services.dnb.de/oai/accessToken~${token}/repository?verb=ListRecords&from=2019-02-18&until=2019-02-18&set=dnb-all:reiheN&metadataPrefix=MARC21-xml
```

* LOC
    * TODO
* BL
    * TODO

### Kategorien (Sachgruppen)

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