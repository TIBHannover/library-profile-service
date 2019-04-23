package eu.tib.profileservice.util;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Temporary solution to export the accepted {@link Document}s to file.
 * This should be replaced by an online solution.
 */
@Component
public class FileExportProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(FileExportProcessor.class);

  private static final String TMP_FILE_PREFIX = "profileservice_export_";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * Delete the export files in the export directory.
   */
  public void cleanupExportFiles() {
    File exportDir = getExportDir();
    if (exportDir != null) {
      for (File file : exportDir.listFiles((dir, name) -> name.startsWith(TMP_FILE_PREFIX))) {
        file.delete();
      }
    }
  }

  /**
   * Read the content of the export file with the given filename.
   * @param exportFileName filename
   * @return content; null, if the content could not be read
   */
  public byte[] getBytesOfExportFile(final String exportFileName) {
    File exportDir = getExportDir();
    if (exportDir != null) {
      List<File> f = Arrays.asList(exportDir.listFiles((dir, name) -> name.equals(exportFileName)));
      if (f.size() > 0) {
        Path path = Paths.get(f.get(0).getAbsolutePath());
        try {
          return Files.readAllBytes(path);
        } catch (IOException e) {
          LOG.error("cannot read file", e);
        }
      }
    }
    LOG.warn("cannot get the content of file {}", exportFileName);
    return null;
  }

  /**
   * List all currently available export files.
   * @return list of files
   */
  public String[] listExportFiles() {
    File exportDir = getExportDir();
    if (exportDir == null) {
      LOG.warn("cannot find the export directory");
      return new String[0];
    } else {
      String[] files = exportDir.list((dir, name) -> name.startsWith(TMP_FILE_PREFIX));
      Arrays.sort(files);
      return files;
    }

  }

  private File getExportDir() {
    File exportDir;
    File exportFile;
    try {
      exportFile = File.createTempFile("profileservice", ".tmp");
      exportDir = exportFile.getParentFile();
      exportFile.delete();
    } catch (IOException e) {
      return null;
    }
    return exportDir;
  }

  /**
   * Export the given {@link Document}s to a new temporary file.
   * @param documents documents to export
   * @throws IOException thrown if there was an error writing the file
   */
  public void export(final List<Document> documents) throws IOException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    String timestamp = LocalDateTime.now().format(formatter);
    File exportFile = File.createTempFile(TMP_FILE_PREFIX + timestamp + "_", ".txt");
    try (FileWriter fw = new FileWriter(exportFile)) {
      for (Document document : documents) {
        fw.write(toExportString(document));
        fw.write(LINE_SEPARATOR);
        fw.write(LINE_SEPARATOR);
        fw.write(LINE_SEPARATOR);
      }
      fw.flush();
    }
  }

  private String toExportString(final Document document) {
    StringBuilder sb = new StringBuilder();
    DocumentMetadata meta = document.getMetadata();
    sb.append(meta.getTitle());
    append(sb, meta.getRemainderOfTitle());
    sb.append(LINE_SEPARATOR);
    sb.append(meta.getAuthors() == null ? "" : String.join("; ", meta.getAuthors()));
    sb.append(LINE_SEPARATOR);
    sb.append(meta.getEdition() == null ? "" : meta.getEdition());
    append(sb, meta.getPublisher());
    append(sb, meta.getPlaceOfPublication());
    append(sb, meta.getDateOfPublication());
    append(sb, meta.getPhysicalDescription());
    append(sb, meta.getSeries());
    sb.append(LINE_SEPARATOR);
    sb.append(String.join(", ", meta.getIsbns()));
    sb.append(LINE_SEPARATOR);
    sb.append(meta.getTermsOfAvailability() == null ? "" : meta.getTermsOfAvailability());

    return sb.toString();
  }

  private void append(final StringBuilder sb, final String text) {
    sb.append(" / ");
    if (text != null) {
      sb.append(text);
    }
  }
}
