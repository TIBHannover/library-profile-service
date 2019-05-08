package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.util.List;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class TibMarcXml2DocumentConverter extends MarcXml2DocumentConverter {

  @Override
  protected DocumentMetadata record2Document(final Record record) {
    DocumentMetadata document = super.record2Document(record);
    List<String> electronicLocation = getAllData(record, "856", 'u', null, '4', null);
    if (electronicLocation.size() > 0) {
      document.setInventoryUri(electronicLocation.get(0));
    }
    document.setInventoryAccessionNumber(getAccessionNumber(record));
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
