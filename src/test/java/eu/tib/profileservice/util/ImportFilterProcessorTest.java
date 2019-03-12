package eu.tib.profileservice.util;

import static org.assertj.core.api.Assertions.assertThat;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.domain.ImportFilter;
import eu.tib.profileservice.domain.ImportFilter.Action;
import eu.tib.profileservice.domain.ImportFilter.ConditionType;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of {@link ImportFilterProcessor}.
 */
public class ImportFilterProcessorTest {

  private ImportFilterProcessor processor;

  private ImportFilter newFilter(final Action action, final String condition,
      final ConditionType conditionType) {
    ImportFilter filter = new ImportFilter();
    filter.setAction(action);
    filter.setCondition(condition);
    filter.setConditionType(conditionType);
    return filter;
  }

  private Document newDocument(final List<String> formKeywords, final Set<String> ddcCategories) {
    final Document document = new Document();
    final DocumentMetadata documentMeta = new DocumentMetadata();
    documentMeta.setTitle("test title");
    documentMeta.setIsbns(Arrays.asList(new String[] {"1234567890"}));
    documentMeta.setFormKeywords(formKeywords);
    documentMeta.setDeweyDecimalClassifications(ddcCategories);
    document.setMetadata(documentMeta);
    document.setCreationDateUtc(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    return document;
  }

  /**
   * Setup test environment.
   */
  @Before
  public void setUp() {
    List<ImportFilter> filterRules = new ArrayList<ImportFilter>();
    filterRules.add(newFilter(Action.IGNORE, ".*(Testbuch|Testbücher).*",
        ConditionType.FORM_KEYWORD));
    filterRules.add(newFilter(Action.IGNORE, "300.*", ConditionType.CATEGORY));
    processor = new ImportFilterProcessor(filterRules);
  }

  @Test
  public void testProcessDocumentMatches() {
    List<String> formKeywords = Arrays.asList(new String[] {"Testbücher"});
    Set<String> ddcCategories = new HashSet<>();
    Document document = newDocument(formKeywords, ddcCategories);

    processor.process(document);
    assertThat(document.getStatus()).isEqualTo(Status.IGNORED);

    formKeywords = new ArrayList<String>();
    ddcCategories = new HashSet<>(Arrays.asList(new String[] {"300.123"}));
    document = newDocument(formKeywords, ddcCategories);
    processor.process(document);
    assertThat(document.getStatus()).isEqualTo(Status.IGNORED);

    formKeywords = Arrays.asList(new String[] {"rotes Testbuch im Test"});
    ddcCategories = new HashSet<>();
    document = newDocument(formKeywords, ddcCategories);
    processor.process(document);
    assertThat(document.getStatus()).isEqualTo(Status.IGNORED);

  }

  @Test
  public void testProcessDocumentWithoutMatch() {
    List<String> formKeywords = Arrays.asList(new String[] {"Test"});
    Document document = newDocument(formKeywords, null);

    processor.process(document);
    assertThat(document.getStatus()).isNull();

    Set<String> ddcCategories = new HashSet<>(Arrays.asList(new String[] {"303"}));
    document = newDocument(null, ddcCategories);
    processor.process(document);
    assertThat(document.getStatus()).isNull();
  }

}
