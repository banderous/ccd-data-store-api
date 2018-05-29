package uk.gov.hmcts.ccd.endpoint.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.ccd.CoreCaseService;
import uk.gov.hmcts.ccd.FieldGenerator;
import uk.gov.hmcts.ccd.ICCDApplication;
import uk.gov.hmcts.ccd.data.casedetails.search.FieldMapSanitizeOperation;
import uk.gov.hmcts.ccd.data.casedetails.search.MetaData;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseEventTrigger;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseView;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewField;
import uk.gov.hmcts.ccd.domain.model.definition.*;
import uk.gov.hmcts.ccd.domain.model.search.*;
import uk.gov.hmcts.ccd.domain.service.aggregated.*;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.*;

@RestController
@RequestMapping(path = "/aggregated",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class QueryEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(QueryEndpoint.class);
    private final AuthorisedGetCaseViewOperation getCaseViewOperation;
    private final GetEventTriggerOperation getEventTriggerOperation;
    private final SearchQueryOperation searchQueryOperation;
    private final FieldMapSanitizeOperation fieldMapSanitizeOperation;
    private final FindSearchInputOperation findSearchInputOperation;
    private final FindWorkbasketInputOperation findWorkbasketInputOperation;
    private final GetCaseTypesOperation getCaseTypesOperation;
    private final HashMap<String, Predicate<AccessControlList>> accessMap;
    private final CoreCaseService application;

    @Inject
    public QueryEndpoint(final AuthorisedGetCaseViewOperation getCaseViewOperation,
                         @Qualifier(AuthorisedGetEventTriggerOperation.QUALIFIER) final GetEventTriggerOperation getEventTriggerOperation,
                         final SearchQueryOperation searchQueryOperation,
                         final FieldMapSanitizeOperation fieldMapSanitizeOperation,
                         @Qualifier(AuthorisedFindSearchInputOperation.QUALIFIER) final FindSearchInputOperation findSearchInputOperation,
                         @Qualifier(AuthorisedFindWorkbasketInputOperation.QUALIFIER) final FindWorkbasketInputOperation findWorkbasketInputOperation,
                         @Qualifier(AuthorisedGetCaseTypesOperation.QUALIFIER) final GetCaseTypesOperation getCaseTypesOperation,
                         CoreCaseService application) {
        this.getCaseViewOperation = getCaseViewOperation;
        this.getEventTriggerOperation = getEventTriggerOperation;
        this.searchQueryOperation = searchQueryOperation;
        this.fieldMapSanitizeOperation = fieldMapSanitizeOperation;
        this.findSearchInputOperation = findSearchInputOperation;
        this.findWorkbasketInputOperation = findWorkbasketInputOperation;
        this.getCaseTypesOperation = getCaseTypesOperation;
        this.accessMap = Maps.newHashMap();
        this.application = application;
        accessMap.put("create", CAN_CREATE);
        accessMap.put("update", CAN_UPDATE);
        accessMap.put("read", CAN_READ);
    }

    /*
     * @deprecated see https://tools.hmcts.net/jira/browse/RDM-1421
     */
    @Deprecated
    @Transactional
    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types", method = RequestMethod.GET)
    @ApiOperation(value = "Get case types")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of case types for the given access criteria"),
        @ApiResponse(code = 404, message = "No case types found for given access criteria")})
    public List<CaseType> getCaseTypes(@PathVariable("jid") final String jurisdictionId,
                                       @RequestParam(value = "access", required = true) String access) {
        return Lists.newArrayList(application.getCaseType());
    }

    @Transactional
    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases", method = RequestMethod.GET)
    @ApiOperation(value = "Get case data with UI layout")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of case data for the given search criteria"),
        @ApiResponse(code = 412, message = "Mismatch between case type and workbasket definitions")})
    public SearchResultView searchNew(@PathVariable("jid") final String jurisdictionId,
                                      @PathVariable("ctid") final String caseTypeId,
                                      @RequestParam java.util.Map<String, String> params) throws IOException {
        String view = params.get("view");
        MetaData metadata = new MetaData(caseTypeId, jurisdictionId);
        metadata.setState(param(params, MetaData.STATE_PARAM));
        metadata.setCaseReference(param(params, MetaData.CASE_REFERENCE_PARAM));
        metadata.setCreatedDate(param(params, MetaData.CREATED_DATE_PARAM));
        metadata.setLastModified(param(params, MetaData.LAST_MODIFIED_PARAM));
        metadata.setSecurityClassification(param(params, MetaData.SECURITY_CLASSIFICATION_PARAM));
        metadata.setPage(param(params, MetaData.PAGE_PARAM));

        Map<String, String> sanitized = fieldMapSanitizeOperation.execute(params);

        return application.search();
    }

    private Optional<String> param(Map<String, String> queryParameters, String param) {
        return Optional.ofNullable(queryParameters.get(param));
    }

    @Transactional
    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/inputs", method = RequestMethod.GET)
    @ApiOperation(value = "Get Search Input details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Search Input data found for the given case type and jurisdiction"),
        @ApiResponse(code = 404, message = "No SearchInput found for the given case type and jurisdiction")
    })
    public SearchInput[] findSearchInputDetails(@PathVariable("uid") final Integer uid,
                                                @PathVariable("jid") final String jurisdictionId,
                                                @PathVariable("ctid") final String caseTypeId) {
        return findSearchInputOperation.execute(jurisdictionId, caseTypeId, CAN_READ).toArray(new SearchInput[0]);
    }

    @Transactional
    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/work-basket-inputs", method = RequestMethod.GET)
    @ApiOperation(value = "Get Workbasket Input details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Workbasket Input data found for the given case type and jurisdiction"),
        @ApiResponse(code = 404, message = "No Workbasket Input found for the given case type and jurisdiction")
    })
    public WorkbasketInput[] findWorkbasketInputDetails(@PathVariable("jid") final String jurisdictionId,
                                                        @PathVariable("ctid") final String caseTypeId) {
        return application.getWorkBasketInputs();
    }

    @Transactional
    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}", method = RequestMethod.GET)
    @ApiOperation(value = "Fetch a case for display")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A displayable case")
    })
    public CaseView findCase(@PathVariable("jid") final String jurisdictionId,
                             @PathVariable("ctid") final String caseTypeId,
                             @PathVariable("cid") final String cid) {
        Instant start = Instant.now();
        CaseView caseView = getCaseViewOperation.execute(jurisdictionId, caseTypeId, cid);
        final Duration between = Duration.between(start, Instant.now());
        LOG.warn("findCase has been completed in {} millisecs...", between.toMillis());
        return caseView;
    }

    @Transactional
    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/event-triggers/{etid}", method = RequestMethod.GET)
    @ApiOperation(value = "Fetch an event trigger in the context of a case type")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Empty pre-state conditions"),
        @ApiResponse(code = 422, message = "The case status did not qualify for the event")
    })
    public CaseEventTrigger getEventTriggerForCaseType(@PathVariable("uid") String userId,
                                                       @PathVariable("jid") String jurisdictionId,
                                                       @PathVariable("ctid") String casetTypeId,
                                                       @PathVariable("etid") String eventTriggerId,
                                                       @RequestParam(value = "ignore-warning", required = false) Boolean ignoreWarning) {
        CaseEventTrigger trigger = new CaseEventTrigger();
        List<CaseViewField> fields = application.getCaseType().getCaseFields().stream().map(x -> {
            CaseViewField vf = new CaseViewField();
            vf.setFieldType(x.getFieldType());
            vf.setId(x.getId());
            vf.setLabel(x.getLabel());
            return vf;
        }).collect(Collectors.toList());
        trigger.setId(casetTypeId);
        trigger.setName(casetTypeId);
        trigger.setCaseFields(fields);
        trigger.setCaseId(casetTypeId);
        List<WizardPage> pages = application.getCaseType().getCaseFields().stream().map(x -> {
           WizardPage page = new WizardPage();
           page.setId(x.getId());
           WizardPageField pageField = new WizardPageField();
           pageField.setCaseFieldId(x.getId());
           page.setWizardPageFields(Lists.newArrayList(pageField));
           return page;
        }).collect(Collectors.toList());
        trigger.setWizardPages(pages);
        return trigger;
    }

    @Transactional
    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/event-triggers/{etid}", method = RequestMethod.GET)
    @ApiOperation(value = "Fetch an event trigger in the context of a case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Valid pre-state conditions")
    })
    public CaseEventTrigger getEventTriggerForCase(@PathVariable("jid") String jurisdictionId,
                                                   @PathVariable("ctid") String caseTypeId,
                                                   @PathVariable("cid") String caseId,
                                                   @PathVariable("etid") String eventTriggerId,
                                                   @RequestParam(value = "ignore-warning", required = false) Boolean ignoreWarning) {

        return getEventTriggerOperation.executeForCase(1,
                                                       jurisdictionId,
                                                       caseTypeId,
                                                       caseId,
                                                       eventTriggerId,
                                                       ignoreWarning);
    }
}
