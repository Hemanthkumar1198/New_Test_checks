package voca;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.vocalink.bacs.core.config.ConfigConstants;
import com.vocalink.bacs.core.config.ConfigManager;
import com.vocalink.bacs.core.signature.SignatureProvider;
import com.vocalink.bacs.core.signature.impl.SignatureManager;
import com.vocalink.bacs.core.spring.AppContext;
import com.vocalink.bacs.test.exception.SignAndSubmitException;
import com.vocalink.bacs.ui.BrowserException.ElementInteractionException;
import com.vocalink.bacs.util.PlaywrightFactory;
import com.vocalink.bacs.utility.ScenarioContext;
import lombok.SneakyThrows;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.vocalink.bacs.ui.BrowserException.ExceptionDetails.EMBED_TAG_NO_IN_DATA_ATTRIBUTE;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SignAndSubmit extends PlaywrightFactory {

    private static final Logger LOGGER = LogManager.getLogger(SignAndSubmit.class);
    private static final String PSW_URL = "https://testservices-tt.bacs.co.uk/";
    private static final String RD_SIGN_AND_SUBMIT = "rdserv/signAndSubmit.do";

    Page page;
    private final ConfigManager configManager = ConfigManager.getInstance();
    private final SignatureProvider signatureProvider = new SignatureManager();
    private final ScenarioContext scenarioContext = AppContext.getContext().getBean(ScenarioContext.class);

    private static final String APPID_KEY = "Approval ID";

    @SneakyThrows
    private void signAndSubmitWithReferer(String path, String params, String referer) {
        if (isSignAndSubmitDisabled()) {
            return;
        }
        final String contactDn = (String) scenarioContext.get("currentUser");
        final String signedData = createSignature(contactDn);
        final String baseUrl = configManager.getSutConfig().getProperty(ConfigConstants.APPLICATION_BASE_URL);
        final String queryParams = "ctx=/" + getCtx() + "/&" + params;
        final String url = baseUrl + path + "?" + queryParams;

        // Create request body with signature
        RequestOptions reqOptions = RequestOptions.create()
                .setHeader("Referer", SignAndSubmit.PSW_URL + getCtx() + referer)
                .setForm(FormData.create()
                        .set("Signature", signedData)
                );

        sendRequest(url, reqOptions);
    }

    @SneakyThrows
    private void signAndSubmit(String path, String params) {
        if (isSignAndSubmitDisabled()) {
            return;
        }

        final String contactDn = (String) scenarioContext.get("currentUser");
        final String signedData = createSignature(contactDn);
        final String baseUrl = configManager.getSutConfig().getProperty(ConfigConstants.APPLICATION_BASE_URL);
        final String queryParams = "ctx=/" + getCtx() + "/&" + params;
        final String url = baseUrl + path + "?" + queryParams;

        // Create request body with signature
        RequestOptions reqOptions = RequestOptions.create()
                .setForm(FormData.create()
                        .set("Signature", signedData)
                );

        sendRequest(url, reqOptions);
    }

    public void signAndSubmitContact() {
        signAndSubmitWithReferer("/rdserv/signAndSubmit.do", "", "/refdata/contact/contactsummary/addsmcontact");
    }

    public void signAndSubmitCisaInitiate() {
        signAndSubmit("/isauiserv/initiate/summarysignandsubmit.do", "flowId=cisainit");
    }

    public void signAndSubmitCisaUpload() {
        signAndSubmit("/isauiserv/authenticateAndPersist.do", "flowId=cisaupload");
    }

    public void signAndSubmitCisaApproveWorklist() {
        signAndSubmit("/isauiserv/approve.do", "flowId=cisaapprove&appItem=" + scenarioContext.get(APPID_KEY));
    }

    public void signAndSubmitCisaRejectWorklist() {
        signAndSubmit("/isauiserv/reject.do", "flowId=cisareject&cmt=qwqw&appItem=" + scenarioContext.get(APPID_KEY));
    }

    public void signAndSubmitCisaStatusUpdate() {
        signAndSubmit("/isauiserv/statusupdate/signandsubmit.do", "flowId=cisastat");
    }

    public void signAndSubmitACSWStatusUpdate() {
        signAndSubmit("/acswuiserv/statusupdate/signandsubmit.do", "flowId=acswstatusupdate");
    }

    public void signAndSubmitACSWApprove() {
        signAndSubmit("/acswuiserv/approve.do", "flowId=acswapprove&cmt=Approve&appItem=" + scenarioContext.get(APPID_KEY));
    }

    public void signAndSubmitACSWRejectSwitch() {
        signAndSubmit("/acswuiserv/reject/signandsubmit.do", "flowId=acswswitchreject");
    }

    public void signAndSubmitACSWReject() {
        signAndSubmit("/acswuiserv/reject.do", "flowId=acswreject&cmt=Reject&appItem=" + scenarioContext.get(APPID_KEY));
    }

    public void signAndSubmitACSWUpdateRepairWorklist() {
        signAndSubmit("/acswuiserv/statusupdate/signandsubmit.do", "flowId=acswrepair&repairId=" + scenarioContext.get(APPID_KEY));
    }

    public void signAndSubmitACSWInitiateRepairWorklist() {
        signAndSubmit("/acswuiserv/initiate/signandsubmit.do", "flowId=acswrepair&repairId=" + scenarioContext.get(APPID_KEY));
    }

    public void signAndSubmitACSWAccept() {
        signAndSubmit("/acswuiserv/accept/signandsubmit.do", "flowId=acswswitchaccept");
    }

    public void signAndSubmitACSWBPRS() {
        signAndSubmit("/acswuiserv/bprsauthenticateAndPersist.do", "flowId=bprsupload");
    }

    public void signAndSubmitACSWAuthenticateAndPersist() {
        signAndSubmit("/acswuiserv/authenticateAndPersist.do", "flowId=acswupload");
    }

    public void signAndSubmitACSWInitiate() {
        signAndSubmit("/acswuiserv/initiate/signandsubmit.do", "flowId=acswinit");
    }

    public void signAndSubmitACSWCancel() {
        signAndSubmit("/acswuiserv/cancel/signandsubmit.do", "flowId=acswcancelpayment");
    }

    public void signAndSubmitPEMProcessReferral() {
        signAndSubmit("/pemuiserv/referral/process:commit.do", "flowId=pemreferralWorkList");
    }

    public void signAndSubmitPEMDoNotProcessReferral() {
        signAndSubmit("/pemuiserv/referral/doNotProcess:commit.do", "flowId=pemreferralWorkList");
    }

    public void signAndSubmitPEMExtract() {
        signAndSubmit("/pemuiserv/subcompman/extract/confirm.do", "flowId=pemsubmissionmanagement");
    }

    public void signAndSubmitPEMAmendCheck() {
        signAndSubmit("/pemuiserv/subcompman/amendDate/confirm.do", "flowId=pemsubmissionmanagement");
    }

    public void signAndSubmitPEMReInput() {
        signAndSubmit("/pemuiserv/subcompman/reinput/confirm.do", "flowId=pemsubmissionmanagement");
    }

    public void signAndSubmitPEMAcknowledgeReferral() {
        signAndSubmit("/pemuiserv/referral/acknowledge:commit.do", "flowId=pemreferralWorkList");
    }

    public void signAndSubmitPEMCreateReferral() {
        signAndSubmit("/pemuiserv/manualReferral/create:commit.do", "flowId=pemmanualReferral");
    }

    public void signAndSubmitPEMApproveWorkList() {
        signAndSubmit("/pemuiserv/approval/approveItem/confirm.do", "flowId=pemapprovalWorkList");
    }

    public void signAndSubmitPEMRejectWorkList() {
        signAndSubmit("/pemuiserv/approval/rejectItem/confirm.do", "flowId=pemapprovalWorkList");
    }

        public void signAndSubmitPEMSubmit() {
        signAndSubmit("/pemuiserv/subcompman/submit/confirm.do", "flowId=pemsubmissionmanagement");
    }

    public void signAndSubmitPEMCancel() {
        signAndSubmit("/pemuiserv/subcompman/cancel/confirm.do", "flowId=pemsubmissionmanagement");
    }

    private String getCtx() {
        String url = page.url();
        String ctx = "";
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            String[] segments = path.split("/");
            if (segments.length > 1) {
                ctx = segments[1];
            }
        } catch (Exception e) {
            LOGGER.error("Error while extracting ctx from url: " + url, e);
        }
        return ctx;
    }

    private String createSignature(String contactDn) {
        String dataToSign = getDataToSign();
        return signatureProvider.sign(dataToSign, contactDn);
    }

    private String getDataToSign() {
        String data = "";
        try {
            Locator embedTag = page.locator("embed");
            if (embedTag.count() > 0) {
                String srcAttribute = embedTag.getAttribute("src");
                if (srcAttribute != null && srcAttribute.contains("data=")) {
                    String[] parts = srcAttribute.split("data=");
                    if (parts.length > 1) {
                        data = URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name());
                    }
                } else {
                    String dataAttribute = embedTag.getAttribute("data");
                    if (dataAttribute != null) {
                        data = URLDecoder.decode(dataAttribute, StandardCharsets.UTF_8.name());
                    } else {
                        throw new ElementInteractionException(EMBED_TAG_NO_IN_DATA_ATTRIBUTE);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while getting data to sign: ", e);
            throw new SignAndSubmitException("Exception while getting data to sign: ", e);
        }
        return data;
    }

    private void sendRequest(String url, RequestOptions reqOptions) throws HttpResponseException {
        APIRequestContext requestContext = page.context().request();
        APIResponse response = requestContext.post(url, reqOptions);
        if (response.status() == 200) {
            LOGGER.info("Sign and Submit request submitted successfully.");
            String responseText = response.text();
            page.setContent(responseText);
        } else {
            LOGGER.error("Failed to sign and submit. Response Status: " + response.status());
            throw new HttpResponseException(response.status(), "Failed to sign and submit.");
        }
    }

    private boolean isSignAndSubmitDisabled() {
        String isSignAndSubmitDisabled = configManager.getSutConfig().getProperty("disableSignAndSubmit");
        if (isSignAndSubmitDisabled != null && isSignAndSubmitDisabled.equalsIgnoreCase("true")) {
            LOGGER.info("Sign and submit is disabled in configuration.");
            return true;
        }
        return false;
    }

     public void signAndSubmitAServicesConfirm() { signAndSubmit("pswserv/confirm.do", "flowId=aservicesubmit"); }

	    public void signAndSubmitAServicesReject() { signAndSubmit("pswserv/reject.do", "flowId=aservicereject"); }

	    public void signAndSubmitDDICSubmitChallenge() { // 1 usage Jean-Benoit Semichon
	        signAndSubmit("pswserv/confirmChallenge.do", "flowId=submitchallenge");
	    }

	    public void signAndSubmitDDICAcceptChallenge() { // 1 usage Jean-Benoit Semichon
	        signAndSubmit("pswserv/acceptDdicChallengeConfirm.do", "flowId=acceptddicchallenge");
	    }

	    public void signAndSubmitDDICRejectChallenge() { // 1 usage Jean-Benoit Semichon
	        signAndSubmit("pswserv/rejectDdicChallengeConfirm.do", "flowId=rejectddicchallenge");
	    }

	    public void signAndSubmitDDICR7Creation() { // 1 usage Jean-Benoit Semichon
	        signAndSubmit("pswserv/createReasonCodeConfirm.do", "flowId=createreasoncode");
	    }

	    public void signAndSubmitDDICR7Reject() { // 3 usages Jean-Benoit Semichon
	        signAndSubmit("pswserv/rejectDdicReasonCodeConfirm.do", "flowId=rejectddicReasonCode");
	    }

	    public void signAndSubmitDDICR7RejectForRepair() { // 1 usage Jean-Benoit Semichon
	        signAndSubmit("pswserv/rejectForRepairDdicReasonCodeConfirm.do", "flowId=rejectForRepairddicReasonCode");
	    }

	    public void signAndSubmitDDICR7Accept() { // 3 usages Jean-Benoit Semichon
	        signAndSubmit("pswserv/acceptDdicReasonCodeConfirm.do", "flowId=acceptDdicReasonCodeConfirm");
	    }

	    public void signAndSubmitRefDataMaintain() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=mntn"); }
	    
	        public void signAndSubmitAServicesConfirm() { signAndSubmit("pswserv/confirm.do", "flowId=aservicesubmit"); }

	        public void signAndSubmitAServicesReject() { signAndSubmit("pswserv/reject.do", "flowId=aservicereject"); }

	        public void signAndSubmitDDICSubmitChallenge() { // 1 usage Jean-Benoit Semichon
	            signAndSubmit("pswserv/confirmChallenge.do", "flowId=submitchallenge");
	        }

	        public void signAndSubmitDDICAcceptChallenge() { // 1 usage Jean-Benoit Semichon
	            signAndSubmit("pswserv/acceptDdicChallengeConfirm.do", "flowId=acceptddicchallenge");
	        }

	        public void signAndSubmitDDICRejectChallenge() { // 1 usage Jean-Benoit Semichon
	            signAndSubmit("pswserv/rejectDdicChallengeConfirm.do", "flowId=rejectddicchallenge");
	        }

	        public void signAndSubmitDDICR7Creation() { // 1 usage Jean-Benoit Semichon
	            signAndSubmit("pswserv/createReasonCodeConfirm.do", "flowId=createreasoncode");
	        }

	        public void signAndSubmitDDICR7Reject() { // 3 usages Jean-Benoit Semichon
	            signAndSubmit("pswserv/rejectDdicReasonCodeConfirm.do", "flowId=rejectddicReasonCode");
	        }

	        public void signAndSubmitDDICR7RejectForRepair() { // 1 usage Jean-Benoit Semichon
	            signAndSubmit("pswserv/rejectForRepairDdicReasonCodeConfirm.do", "flowId=rejectForRepairddicReasonCode");
	        }

	        public void signAndSubmitDDICR7Accept() { // 3 usages Jean-Benoit Semichon
	            signAndSubmit("pswserv/acceptDdicReasonCodeConfirm.do", "flowId=acceptDdicReasonCodeConfirm");
	        }

	        public void signAndSubmitRefDataMaintain() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=mntn"); }

	            public void signAndSubmitRefDataRegisterServiceUser() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=registerSU"); }

	            public void signAndSubmitRefDataRegisterBankOffice() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=registerBO"); }

	            public void signAndSubmitRefDataMaintainBankOffice() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=maintainBO"); }

	            public void signAndSubmitRefDataTransferBankOffice() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=transferBacsBO"); }

	            public void signAndSubmitRefDataApproveReferral() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=refApprove"); }

	            public void signAndSubmitRefDataRejectReferral() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=refReject"); }

	            public void signAndSubmitRefDataAddContact() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=addContact"); }

	            public void signAndSubmitRefDataMaintainAddContact() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=mtnAddContact"); }

	            public void signAndSubmitRefDataMaintainAddServiceUser() { // 7 usages Jean-Benoit Semichon
	                signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=maintainaddsuc");
	            }

	            public void signAndSubmitRefDataSystemNotice() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=systemNotices"); }

	            public void signAndSubmitRefDataGlobal() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=glo"); }

	            /**
	             * Check for HTML encoded character at the given position of the given string.
	             * The start value should be the position of a character triple to check for an HTML
	             * encoded character. If the HTML triple is valid, the character at start position
	             * is returned, otherwise an HTML encoded space ('%20') is returned.
	             *
	             * @param data String to extract potential HTML encoded character from.
	             * @param start Position of the first character of the potential encoded HTML character.
	             * @return %20 if character triple beginning at start is an invalid HTML character.
	             * Character at start position otherwise.
	             */
	            public void signAndSubmitRefDataCreateMajorAccountRedirection() { // 1 usage Jean-Benoit Semichon
	                signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=createMAR");
	            }

	            public void signAndSubmitRefDataMaintainMajorAccountRedirection() { // 2 usages Jean-Benoit Semichon
	                signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=maintainMAR");
	            }

	            public void signAndSubmitRefDataRegisterMessagingUser() { // 4 usages Jean-Benoit Semichon
	                signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=regmainmessuser");
	            }

	            public void signAndSubmitRefDataSwiftBic() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=swiftBic"); }

	            public void signAndSubmitRefDataSchemeParticipation() { // 1 usage Jean-Benoit Semichon
	                signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=schemeParticipation");
	            }

	            public void signAndSubmitRefDataSolutionSupplier() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=solsup"); }

	            public void signAndSubmitRefDataTransferOutSU() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=transferSUOut"); }

	            public void signAndSubmitRefDataSMAddStream() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=smbacsaddstrm"); }

	            public void signAndSubmitRefDataDeleteStream() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=scrn"); }

	            public void signAndSubmitRefDataTransferSU() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=transferSUIn"); }

	            public void signAndSubmitRefDataPaymentUser() { signAndSubmit(RD_SIGN_AND_SUBMIT, "flowId=puser"); }

	            /**
	             * Send a sign and submit request for reject for repair with a given comment
	             * Spaces within the comment will be automatically encoded to %20.
	             */

	            public void signAndSubmitACSWRejectForRepair(final String comment) { // 3 usages Jean-Benoit Semichon
	                final String encodedComment = replaceSpaces(comment);
	                signAndSubmit("acswuiserv/rejectForRepair.do", "flowId=acswrejectforrepair&cmt=" + encodedComment + "&appItem=" + scenarioContext.get(APPID_KEY));
	            }

	            private boolean isSignAndSubmitDisabled() { // 2 usages Jean-Benoit Semichon
	                return !Boolean.parseBoolean(System.getProperty("UseSoftCertificates"));
	            }

	            /**
	             * Create base sign and submit request with relevant data and page cookies.
	             * @return Base request to be sent for sign and submit
	             */
	            private APIRequestContext createAPIRequest() { // 1 usage Jean-Benoit Semichon
	                return getPlaywright().request().newContext(
	                    new APIRequest.NewContextOptions()
	                        .setStorageState(getBrowserContext().storageState())
	                        .setIgnoreHTTPSErrors(true)
	                        .setProxy("http://webaccess2.office.local:8080")
	                );
	            }

	            /**
	             * Create a signature to be used in sign and submit process.
	             * Signature is created using the data to be signed present on a page.
	             *
	             * @param contactDn Contact distinguished name of the user attempting to log in
	             * @return base64 encoded signed data String
	             */
	            private String createSignature(String contactDn) { // 2 usages Jean-Benoit Semichon +1
	                page = (Page) scenarioContext.get("page");
	                String inData = page.locator("//embed").getAttribute("datatobesigned");
	                if (inData == null) {
	                    throw new ElementInteractionException(EMBED_TAG_NO_IN_DATA_ATTRIBUTE, new Exception("Embedded attribute not found"));
	                }
	                String decodedData = decode(inData);
	                return signatureProvider.generateSignature(decodedData, contactDn, false);
	            }

	            /**
	             * Fetches the ctx value from the base url
	             *
	             * @return ctx value from the url
	             */
	            @SneakyThrows // 3 usages Jean-Benoit Semichon
	            private String getCtx() {
	                final URI baseUri = new URI(configManager.getSutConfig().getProperty(ConfigConstants.APPLICATION_BASE_URL));
	                final String[] path = baseUri.getPath().split("/");
	                return path[1];
	            }

	            /**
	             * Sends a request at the given URI and with the given options.
	             * Finds and sets the approval ID if present.
	             *
	             * @param uri The URI to send the request to
	             * @param reqOptions Options to send along with the request
	             * @throws HttpResponseException Thrown if the request does not return a 200 status code.
	             */
	            private void sendRequest(String uri, RequestOptions reqOptions) throws HttpResponseException { // 2 usages Jean-Benoit Semichon
	                final APIRequestContext request = createAPIRequest();
	                APIResponse res = request.post(uri, reqOptions);
	                if (!res.ok()) {
	                    LOGGER.error("Sign and submit request failed with error: {}", res.status());
	                    throw new HttpResponseException(res.status(), "Sign and submit request failed.");
	                }
	            
	                page.navigate(res.url());
	                if (!hasRequestSucceeded(res.url())) {
	                    LOGGER.error("Sign and submit reply status to \"{}\" was unsuccessful.", uri);
	                    throw new SignAndSubmitException("Sign and submit reply status to \"" + uri + "\" was unsuccessful. Got code: " + res.status() +
	                    " and status: \"" + res.statusText() + "\".");
	                }

	                // Set approval ID if present
	                int appId = getAppIdFromURL(res.url());
	                if (appId != -1) {
	                    scenarioContext.set(APPID_KEY, appId);
	                    scenarioContext.addVal(APPID_KEY, appId);
	                }
	            }

	            /**
	             * Retrieve the appId value from a given URL.
	             * If the appId key is not present or has no value then -1 is returned.
	             *
	             * @param url The URL to search for the value of appId key.
	             * @return appId value if present, -1 otherwise.
	             */
	            @SneakyThrows // 1 usage Jean-Benoit Semichon
	            private int getAppIdFromURL(String url) {
	                final URI uri = new URI(url);
	                String[] query = uri.getQuery().split("&");
	                for (String param : query) {
	                    String[] pairing = param.split("=");
	                    if (pairing[0].equals("appId") && pairing.length == 2) {
	                        return Integer.parseInt(pairing[1]);
	                    }
	                }
	                return -1;
	            }

	            /**
	             * Check the status field of the given url to see if a request has succeeded.
	             *
	             * @param url URL to check
	             * @return True if the status field is present and has value "succeed". False otherwise.
	             */
	            @SneakyThrows // 1 usage Jean-Benoit Semichon
	            private boolean hasRequestSucceeded(String url) {
	                final URI uri = new URI(url);
	                String[] query = uri.getQuery().split("&");
	                for (String param : query) {
	                    String[] pairing = param.split("=");
	                    if (pairing[0].equals("status") && pairing.length == 2) {
	                        return pairing[1].equals("success");
	                    }
	                }
	                return false;
	            }

	            /**
	             * Replace all spaces in the given string with %20
	             *
	             * @param str String to replace the spaces of
	             * @return A new string similar to the input but with all spaces replaced with %20
	             */
	            private String replaceSpaces(String str) { // 1 usage Jean-Benoit Semichon
	                StringBuilder converted = new StringBuilder();
	                for (int i = 0; i < str.length(); i++) {
	                    converted.append(str.charAt(i) == ' ' ? "%20" : str.charAt(i));
	                }
	                return converted.toString();
	            }

	            
	                /**
	                 * Decode a given HTML string. If decoding fails, any potential HTML
	                 * character's (a character triple beginning with a '%'), validity will
	                 * be checked. If a character triple is found to be an invalid HTML
	                 * character, the '%' will be replaced by a valid HTML encoded character.
	                 *
	                 * @param inData Data to decode
	                 * @return The decoded HTML data where invalid HTML encoded characters where repla
	                 */
	                private String decode(String inData) { // 1 usage Jean-Benoit Semichon
	                    try {
	                        return URLDecoder.decode(inData, StandardCharsets.UTF_8);
	                    }
	                    catch (IllegalArgumentException e) {
	                        StringBuilder validEncodedData = new StringBuilder();
	                        // Check if error is due to data containing invalid HTML character
	                        for (int i = 0; i < inData.length(); i++) {
	                            validEncodedData.append(checkPotentialHTMLEncodedCharacter(inData, i));
	                        }
	                        return URLDecoder.decode(validEncodedData.toString(), StandardCharsets.UTF_8);
	                    }
	                }

	                /**
	                 * Check for HTML encoded character at the given position of the given string.
	                 * The start value should be the position of a character triple to check for an HTML
	                 * encoded character. If the HTML triple is valid, the character at start position
	                 * is returned, otherwise an HTML encoded space ('%20') is returned.
	                 *
	                 * @param data String to extract potential HTML encoded character from.
	                 * @param start Position of the first character of the potential encoded HTML character.
	                 * @return %20 if character triple beginning at start is an invalid HTML character.
	                 * Character at start position otherwise.
	                 */
	                private String checkPotentialHTMLEncodedCharacter(String data, int start) { // 1 usage Jean-Benoit
	                    // Skip check if this can't be the start of an HTML encoded character
	                    if (data.charAt(start) != '%') {
	                        return data.substring(start, start + 1);
	                    }
	                    try {
	                        int length = Math.min(start + 3, data.length());  //IN CASE % IS NEAR THE END OF STRING
	                        URLDecoder.decode(data.substring(start, length), StandardCharsets.UTF_8);
	                        return "%";
	                    }
	                    catch (IllegalArgumentException e) {
	                        // Found the failing character so replace with valid HTML encoding
	                        return "%20";
	                    }
	                }
	    }

}

}
