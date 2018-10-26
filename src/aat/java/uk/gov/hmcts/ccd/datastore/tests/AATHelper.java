package uk.gov.hmcts.ccd.datastore.tests;

import uk.gov.hmcts.ccd.datastore.tests.helper.CCDHelper;
import uk.gov.hmcts.ccd.datastore.tests.helper.S2SHelper;
import uk.gov.hmcts.ccd.datastore.tests.helper.idam.IdamHelper;
import uk.gov.hmcts.ccd.datastore.tests.helper.idam.OAuth2;

public enum AATHelper {

    INSTANCE;

    private static final long DEFAULT_LOGSTASH_READ_DELAY_MILLIS = 5000;

    private final IdamHelper idamHelper;
    private final S2SHelper s2SHelper;
    private final CCDHelper ccdHelper;

    AATHelper() {
        idamHelper = new IdamHelper(getIdamURL(), OAuth2.INSTANCE);
        s2SHelper = new S2SHelper(getS2SURL(), getGatewayServiceSecret(), getGatewayServiceName());
        ccdHelper = new CCDHelper();
    }

    public String getTestUrl() {
        return Env.require("TEST_URL");
    }

    public String getIdamURL() {
        return Env.require("IDAM_URL");
    }

    public String getS2SURL() {
        return Env.require("S2S_URL");
    }

    public String getGatewayServiceName() {
        return Env.require("CCD_GW_SERVICE_NAME");
    }

    public String getGatewayServiceSecret() {
        return Env.require("CCD_GW_SERVICE_SECRET");
    }

    public IdamHelper getIdamHelper() {
        return idamHelper;
    }

    public S2SHelper getS2SHelper() {
        return s2SHelper;
    }

    public CCDHelper getCcdHelper() {
        return ccdHelper;
    }

    public String getCaseworkerAutoTestEmail() {
        return Env.require("CCD_CASEWORKER_AUTOTEST_EMAIL");
    }

    public String getCaseworkerAutoTestPassword() {
        return Env.require("CCD_CASEWORKER_AUTOTEST_PASSWORD");
    }

    public String getDefinitionStoreUrl() {
        return Env.require("CCD_DEFINITION_STORE_URL");
    }

    public String getImporterAutoTestEmail() {
        return Env.require("CCD_IMPORT_AUTOTEST_EMAIL");
    }

    public String getImporterAutoTestPassword() {
        return Env.require("CCD_IMPORT_AUTOTEST_PASSWORD");
    }

    public String getPrivateCaseworkerEmail() {
        return Env.require("CCD_PRIVATE_CASEWORKER_EMAIL");
    }

    public String getPrivateCaseworkerPassword() {
        return Env.require("CCD_PRIVATE_CASEWORKER_PASSWORD");
    }

    public String getRestrictedCaseworkerEmail() {
        return Env.require("CCD_RESTRICTED_CASEWORKER_EMAIL");
    }

    public String getRestrictedCaseworkerPassword() {
        return Env.require("CCD_RESTRICTED_CASEWORKER_PASSWORD");
    }

    public String getPrivateCaseworkerSolicitorEmail() {
        return Env.require("CCD_PRIVATE_CASEWORKER_SOLICITOR_EMAIL");
    }

    public String getPrivateCaseworkerSolicitorPassword() {
        return Env.require("CCD_PRIVATE_CASEWORKER_SOLICITOR_PASSWORD");
    }

    public String getElasticsearchBaseUri() {
        return Env.require("ELASTIC_SEARCH_SCHEME") + "://" + Env.require("ELASTIC_SEARCH_HOST") + ":" + Env.require("ELASTIC_SEARCH_PORT");
    }

    public Long getLogstashReadDelay() {
        try {
            return Long.valueOf(Env.require("LOGSTASH_READ_DELAY_MILLIS"));
        } catch (NullPointerException e) {
            return DEFAULT_LOGSTASH_READ_DELAY_MILLIS;
        }
    }

}
