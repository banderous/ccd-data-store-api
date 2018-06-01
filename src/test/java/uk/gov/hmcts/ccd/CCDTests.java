package uk.gov.hmcts.ccd;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewField;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTab;
import uk.gov.hmcts.ccd.domain.model.definition.CaseField;
import uk.gov.hmcts.ccd.domain.model.search.SearchResultView;
import uk.gov.hmcts.ccd.domain.model.search.SearchResultViewItem;
import uk.gov.hmcts.ccd.types.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class CCDTests {

    CoreCaseService service;

    @Before
    public void setup() {
        FakeCCDImplementation impl = new FakeCCDImplementation();
        service = new CoreCaseService(new CCDAppConfig(), impl);
    }

    @Test
    public void generatesCaseField() {
        List<CaseField> result = ReflectionUtils.generateFields(TwoFields.class);
        assertThat(result.size()).isEqualTo(1);
        CaseField first = result.get(0);
        assertThat(first.getFieldType().getType()).isEqualTo("Text");
        assertThat(first.getId()).isEqualTo("fooBar");
        assertThat(first.getLabel()).isEqualTo("Foo Bar");
    }

    @Test
    public void extractsSubClassFields() {
        List<CaseField> result = ReflectionUtils.generateFields(WithSubClass.class);
        assertThat(result.size()).isEqualTo(2);
        CaseField first = result.get(0);
        assertThat(first.getFieldType().getType()).isEqualTo("Text");
        assertThat(first.getId()).isEqualTo("fooBar");
        assertThat(first.getLabel()).isEqualTo("Foo Bar");

        CaseField second = result.get(1);
        assertThat(second.getFieldType().getType()).isEqualTo("Text");
        assertThat(second.getId()).isEqualTo("sub");
        assertThat(second.getLabel()).isEqualTo("Sub!");
    }

    @Test
    public void getsView() {
        WithSubClass w = new WithSubClass();
        w.subClass = new SubClass();
        Map<String, Object> result = ReflectionUtils.getCaseView(w);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(ImmutableMap.of(
            "fooBar", "foo",
            "sub", "bar"
        ));
    }

    @Test
    public void extractsCaseStates() {
        ImmutableSet<? extends Enum> states = ReflectionUtils.extractStates(FakeCase.class);
        assertThat(states).isEqualTo(ImmutableSet.copyOf(
            FakeState.values()
        ));
    }

    @Test
    public void mapsComplextype() {
        CaseViewField field = ReflectionUtils.mapComplexType(new TestAddress());
        assertThat(field.getFieldType().getType()).isEqualTo("Complex");
        assertThat(field.getFieldType().getComplexFields().size()).isGreaterThanOrEqualTo(3);
        assertThat(field.getFieldType().getComplexFields().get(0).getLabel()).isEqualTo("test");
    }
    @Test
    public void mapsNestedComplexType() {
        CaseViewField field = ReflectionUtils.mapComplexType(new Party());
        assertThat(field.getFieldType().getType()).isEqualTo("Complex");
        assertThat(field.getFieldType().getComplexFields().size()).isGreaterThanOrEqualTo(2);
        assertThat(field.getFieldType().getComplexFields().get(1).getFieldType().getType()).isEqualTo("Complex");
    }

    @Test
    public void extractsCaseType() {
        assertThat(ReflectionUtils.getCaseType(FakeCCDImplementation.class)).isEqualTo(FakeCase.class);
    }

    @Test
    public void testRhubarbTabView() {
        CaseViewTab[] result = ReflectionUtils.generateCaseViewTabs(new FakeCase("defendantName", "prosecutorName"));
        assertThat(result.length).isEqualTo(3);
        CaseViewTab addressTab = result[0];
        assertThat(addressTab.getFields().length).isEqualTo(1);
        CaseViewField vf = addressTab.getFields()[0];
        assertThat(vf.getFieldType().getComplexFields().size()).isEqualTo(2);
    }

    @Test
    public void searchesCases() {
        SearchResultView view = service.search(Maps.newHashMap());

        assertThat(view.getSearchResultViewColumns().length).isEqualTo(3);
        SearchResultViewItem[] items = view.getSearchResultViewItems();
        assertThat(items.length).isEqualTo(2);
        assertThat(items[0].getCaseFields().get("defendantName").asText()).isEqualTo("Defendant");
    }
}
