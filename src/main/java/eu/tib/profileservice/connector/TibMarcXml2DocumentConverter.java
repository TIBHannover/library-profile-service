package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class TibMarcXml2DocumentConverter extends MarcXml2DocumentConverter {

  @Override
  protected DocumentMetadata record2Document(final Record record) {
    DocumentMetadata document = super.record2Document(record);
    List<String> picaProductionNumber = getAllData(record, "024", 'a', "ppn", '7', null);
    if (picaProductionNumber.size() > 0) {
      String ppn = picaProductionNumber.get(0);
      Map<String, String> inventoryUris = new HashMap<>();
      String portalUri = "https://www.tib.eu/de/suchen/id/TIBKAT%3A" + ppn;
      inventoryUris.put(portalUri, "Portal");
      String accessionNumber = getAccessionNumber(record);
      if (accessionNumber == null) {
        accessionNumber = "OPAC";
      }
      String opacUri = "https://opac.tib.eu/DB=1/LNG=DU/XMLPRS=N/PPN?PPN=" + ppn;
      inventoryUris.put(opacUri, accessionNumber);
      document.setInventoryUris(inventoryUris);
    }
    return document;
  }

  private String getAccessionNumber(final Record record) {
    List<VariableField> fields = record.getVariableFields("900");
    for (VariableField field : fields) {
      if (field instanceof DataField) {
        Subfield subfieldB = ((DataField) field).getSubfield('b');
        Subfield subfieldD = ((DataField) field).getSubfield('d');
        if (subfieldB != null && subfieldD != null && "TIBKAT".equals(subfieldB.getData())) {
          return subfieldD.getData();
        }
      }
    }
    return null;
  }

}
