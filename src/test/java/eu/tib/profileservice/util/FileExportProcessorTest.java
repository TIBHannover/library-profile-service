package eu.tib.profileservice.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Test for {@link FileExportProcessor}.
 */
public class FileExportProcessorTest {

  private Document newDocument() {
    final Document document = new Document();
    final DocumentMetadata documentMeta = new DocumentMetadata();
    documentMeta.setTitle("test title");
    documentMeta.setIsbns(Arrays.asList(new String[] {"1234567890"}));
    documentMeta.setEdition("edition");
    documentMeta.setSeries("series");
    document.setMetadata(documentMeta);
    document.setCreationDateUtc(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    return document;
  }

  @Test
  public void testFileExport() throws IOException {
    FileExportProcessor processor = new FileExportProcessor();
    List<Document> documents = new ArrayList<>();
    documents.add(newDocument());
    processor.export(documents);

    String[] files = processor.listExportFiles();
    assertNotNull(files);
    assertThat(files.length).isGreaterThan(0);

    String filename = files[0];
    byte[] content = processor.getBytesOfExportFile(filename);
    assertNotNull(content);
    assertThat(content.length).isGreaterThan(0);

    processor.cleanupExportFiles();

    files = processor.listExportFiles();
    assertNotNull(files);
    assertThat(files.length).isEqualTo(0);

    content = processor.getBytesOfExportFile(filename);
    assertNull(content);
  }

}
