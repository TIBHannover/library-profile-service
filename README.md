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

Implementiert: _TibConnector_, der gegen _https://getinfo.tib.eu/sru_ prüft.

### Filterregeln

**TODO**

Es sollen Regeln erstellt werden können, so dass Titel direkt beim Import abgelehnt werden können.
    
### Cleanup

Die Daten werden nach einem bestimmten Zeitraum wieder aus dem System gelöscht über den _eu.tib.profileservice.scheduling.DocumentCleanupJob_.

**TODO**: dabei keine Titel mit Status "zurückgestellt" löschen.