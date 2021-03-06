package uk.gov.hmcts.ccd.domain.service.search;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.config.JacksonUtils;
import uk.gov.hmcts.ccd.data.casedetails.search.MetaData;
import uk.gov.hmcts.ccd.data.definition.CachedCaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.definition.CaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.user.UserRepository;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.service.common.AccessControlService;
import uk.gov.hmcts.ccd.endpoint.exceptions.ValidationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.CAN_READ;
import static uk.gov.hmcts.ccd.domain.service.search.AuthorisedSearchOperation.QUALIFIER;

@Service
@Qualifier(QUALIFIER)
public class AuthorisedSearchOperation implements SearchOperation {
    public static final String QUALIFIER = "authorised";

    private final SearchOperation searchOperation;
    private final CaseDefinitionRepository caseDefinitionRepository;
    private final AccessControlService accessControlService;
    private final UserRepository userRepository;

    @Autowired
    public AuthorisedSearchOperation(@Qualifier("classified") final SearchOperation searchOperation,
                                     @Qualifier(CachedCaseDefinitionRepository.QUALIFIER)
                                         final CaseDefinitionRepository caseDefinitionRepository,
                                     final AccessControlService accessControlService,
                                     @Qualifier(CachedCaseDefinitionRepository.QUALIFIER)
                                         final UserRepository userRepository) {
        this.searchOperation = searchOperation;
        this.caseDefinitionRepository = caseDefinitionRepository;
        this.accessControlService = accessControlService;
        this.userRepository = userRepository;
    }

    @Override
    public List<CaseDetails> execute(MetaData metaData, Map<String, String> criteria) {

        final List<CaseDetails> results = searchOperation.execute(metaData, criteria);
        CaseTypeDefinition caseTypeDefinition = getCaseType(metaData.getCaseTypeId());
        Set<String> userRoles = getUserRoles();

        return (null == results || !accessControlService.canAccessCaseTypeWithCriteria(caseTypeDefinition, userRoles,
            CAN_READ))
            ? Lists.newArrayList() : filterByReadAccess(results, caseTypeDefinition, userRoles);
    }

    private List<CaseDetails> filterByReadAccess(List<CaseDetails> results, CaseTypeDefinition caseTypeDefinition,
                                                 Set<String> userRoles) {

        return results.stream()
            .filter(caseDetails -> accessControlService.canAccessCaseStateWithCriteria(caseDetails.getState(),
                caseTypeDefinition, userRoles, CAN_READ))
            .collect(Collectors.toList())
            .stream()
            .map(caseDetails -> verifyFieldReadAccess(caseTypeDefinition, userRoles, caseDetails))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    private CaseTypeDefinition getCaseType(String caseTypeId) {
        final CaseTypeDefinition caseTypeDefinition = caseDefinitionRepository.getCaseType(caseTypeId);
        if (caseTypeDefinition == null) {
            throw new ValidationException("Cannot find case type definition for  " + caseTypeId);
        }
        return caseTypeDefinition;

    }

    private Set<String> getUserRoles() {
        Set<String> userRoles = userRepository.getUserRoles();
        if (userRoles == null) {
            throw new ValidationException("Cannot find user roles for the user");
        }
        return userRoles;
    }

    private Optional<CaseDetails> verifyFieldReadAccess(CaseTypeDefinition caseTypeDefinition,
                                                        Set<String> userRoles,
                                                        CaseDetails caseDetails) {

        if (caseTypeDefinition == null || caseDetails == null || CollectionUtils.isEmpty(userRoles)) {
            return Optional.empty();
        }

        caseDetails.setData(JacksonUtils.convertValue(
            accessControlService.filterCaseFieldsByAccess(
                JacksonUtils.convertValueJsonNode(caseDetails.getData()),
                caseTypeDefinition.getCaseFieldDefinitions(),
                userRoles,
                CAN_READ,
                false)));
        caseDetails.setDataClassification(JacksonUtils.convertValue(
            accessControlService.filterCaseFieldsByAccess(
                JacksonUtils.convertValueJsonNode(caseDetails.getDataClassification()),
                caseTypeDefinition.getCaseFieldDefinitions(),
                userRoles,
                CAN_READ,
                true)));

        return Optional.of(caseDetails);
    }
}
