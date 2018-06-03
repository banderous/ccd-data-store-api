package uk.gov.hmcts.ccd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.ccd.domain.model.definition.CaseField;
import uk.gov.hmcts.ccd.types.fields.Address;
import uk.gov.hmcts.ccd.types.fields.HasStringList;
import uk.gov.hmcts.ccd.types.model.FakeCase;
import uk.gov.hmcts.ccd.types.model.FakeView;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ViewGeneratorTest {
    @Test
    public void extractsFields() {
        FakeView view = new FakeView();
        FakeCase fakeCase = new FakeCase("A", "B");
        List<Object> fields = view.render(fakeCase);
        assertThat(fields.size()).isEqualTo(2);
    }

    @Test
    public void convertsString() {
        CaseField field = ViewGenerator.convert("hello");
        assertThat(field.getFieldType().getType()).isEqualTo("Text");
        assertThat(field.getValue().isTextual()).isTrue();
    }

    @Test
    public void convertsComplex() {
        CaseField field = ViewGenerator.convert(new Address());
        assertThat(field.getFieldType().getType()).isEqualTo("Complex");
        assertThat(field.getFieldType().getComplexFields().get(0).getValue().toString()).contains("test line 1");
    }

    @Test
    public void convertsStringList() throws JsonProcessingException {
        CaseField field = ViewGenerator.convert(new HasStringList());
        assertThat(field.getFieldType().getType()).isEqualTo("Complex");
        CaseField collection = field.getFieldType().getComplexFields().get(0);
        assertThat(collection.getFieldType().getType()).isEqualTo("Collection");
        CCDCollectionEntry[] values = new ObjectMapper().treeToValue(collection.getValue(), CCDCollectionEntry[].class);
        assertThat(values.length).isEqualTo(2);
    }
}
