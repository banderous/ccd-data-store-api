package uk.gov.hmcts.ccd.types;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.definition.CaseListField;

@Getter
@Setter
public class TwoFields {
    @CaseListField(label = "Foo Bar")
    private String fooBar;
    private String without;
}
