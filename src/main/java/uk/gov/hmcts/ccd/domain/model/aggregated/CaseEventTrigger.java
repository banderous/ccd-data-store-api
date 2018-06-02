package uk.gov.hmcts.ccd.domain.model.aggregated;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.domain.model.definition.CaseField;
import uk.gov.hmcts.ccd.domain.model.definition.WizardPage;

import java.util.List;

public class CaseEventTrigger {
    private String id;
    private String name;
    private String description;
    @JsonProperty("case_id")
    private String caseId;
    @JsonProperty("case_fields")
    private List<CaseField> caseFields;
    @JsonProperty("event_token")
    private String eventToken;
    @JsonProperty("wizard_pages")
    private List<WizardPage> wizardPages;
    @JsonProperty("show_summary")
    private Boolean showSummary;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public List<CaseField> getCaseFields() {
        return caseFields;
    }

    public void setCaseFields(List<CaseField> caseFields) {
        this.caseFields = caseFields;
    }

    public String getEventToken() {
        return eventToken;
    }

    public void setEventToken(String eventToken) {
        this.eventToken = eventToken;
    }

    public List<WizardPage> getWizardPages() {
        return wizardPages;
    }

    public void setWizardPages(List<WizardPage> wizardPages) {
        this.wizardPages = wizardPages;
    }

    public Boolean getShowSummary() {
        return showSummary;
    }

    public void setShowSummary(final Boolean showSummary) {
        this.showSummary = showSummary;
    }
}
