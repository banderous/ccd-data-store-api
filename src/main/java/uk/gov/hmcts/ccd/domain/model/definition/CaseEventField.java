package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

@ApiModel(description = "")
public class CaseEventField implements Serializable {

    private String caseFieldId = null;
    private String displayContext = null;
    private String showCondition = null;
    private Boolean showSummaryChangeOption = null;

    @ApiModelProperty(required = true, value = "Foreign key to CaseField.id")
    @JsonProperty("case_field_id")
    public String getCaseFieldId() {
        return caseFieldId;
    }

    public void setCaseFieldId(String caseFieldId) {
        this.caseFieldId = caseFieldId;
    }

    @ApiModelProperty(value = "whether this field is optional, mandatory or read only for this event")
    @JsonProperty("display_context")
    public String getDisplayContext() {
        return displayContext;
    }

    public void setDisplayContext(String displayContext) {
        this.displayContext = displayContext;
    }

    @ApiModelProperty(value = "Show Condition expression for this field")
    @JsonProperty("show_condition")
    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    @ApiModelProperty(value = "Show Summary Change Option")
    @JsonProperty("show_summary_change_option")
    public Boolean getShowSummaryChangeOption() {
        return showSummaryChangeOption;
    }

    public void setShowSummaryChangeOption(final Boolean showSummaryChangeOption) {
        this.showSummaryChangeOption = showSummaryChangeOption;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("caseFieldId", caseFieldId)
            .append("displayContext", displayContext)
            .append("showCondition", showCondition)
            .append("showSummaryChangeOption", showSummaryChangeOption)
            .toString();
    }
}
