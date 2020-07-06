package uk.gov.hmcts.ccd.domain.model.search;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;

public class CaseSearchResult {

    public static final CaseSearchResult EMPTY = new CaseSearchResult(0L, emptyList());

    private Long total;
    private List<CaseDetails> cases;
    private List<CaseFieldsAggregationResult> caseFieldsAggregations;

    public CaseSearchResult() {
    }

    public CaseSearchResult(Long total, List<CaseDetails> cases, List<CaseFieldsAggregationResult>  caseFieldsAggregations) {
        this.cases = cases;
        this.total = total;
        this.caseFieldsAggregations = caseFieldsAggregations;
    }

    public CaseSearchResult(Long total, List<CaseDetails> cases) {
        this(total,cases,null);
    }


    public CaseSearchResult(List<CaseFieldsAggregationResult>  caseFieldsAggregations, Long total) {
        this(new Long(0),new ArrayList(),caseFieldsAggregations);
    }

    public List<CaseDetails> getCases() {
        return cases;
    }

    public Long getTotal() {
        return total;
    }

    public List<String> getCaseReferences(String caseTypeId) {
        return cases == null
            ? emptyList()
            : cases.stream().filter(c -> c.getCaseTypeId().equals(caseTypeId)).map(CaseDetails::getReferenceAsString).collect(toList());
    }

    public List<CaseFieldsAggregationResult> getCaseFieldsAggregations() {
        return caseFieldsAggregations;
    }
}
