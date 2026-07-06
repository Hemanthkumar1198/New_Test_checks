package voca.stepdef;

    import io.cucumber.java.en.Given;
    import io.cucumber.java.en.Then;
    import io.cucumber.java.en.When;
    import lombok.SneakyThrows;
    import org.junit.Assert;
    
    import java.text.ParseException;
    import java.util.List;
    
    
    public class CisaStepDef extends BaseAServicesSubmission {
    
        private static final String SIMILAR_TRANSFER_REQUEST_MODAL = "//div[contains(text(), 'The customer details contain duplicate data. Surname, give...
    
        static String user = "";
    
        private final SignAndSubmit signAndSubmit = new SignAndSubmit();
        private final CisaPage cisaPage = new CisaPage(PlaywrightFactory.page);
        private final LoginPage loginPage = new LoginPage(PlaywrightFactory.page);
        private final ServicesPage servicesPage = new ServicesPage(PlaywrightFactory.page);
        private final DashboardPage dashboardPage = new DashboardPage(PlaywrightFactory.page);
        private final OperationsPage operationsPage = new OperationsPage(PlaywrightFactory.page);
        private final ActivityLogsPage activityLogsPage = new ActivityLogsPage(PlaywrightFactory.page);
        private final EventDetailsPage eventDetailsPage = new EventDetailsPage(PlaywrightFactory.page);
        private final CISATransferListPage cisaTransferList = new CISATransferListPage(PlaywrightFactory.page);
        private final InitiateTransferSummary cisaSummary = new InitiateTransferSummary(PlaywrightFactory.page);
        private final InitiateCashISATransferPage cisaInitiate = new InitiateCashISATransferPage(PlaywrightFactory.page);
        private final CISATransferDetailsPage cisaTransferDetailsPage = new CISATransferDetailsPage(PlaywrightFactory.page);
        private final CISAApprovalWorklistPage cisaApprovalWorklistPage = new CISAApprovalWorklistPage(PlaywrightFactory.page);
        private final SwitchApprovalConfirmationScreen confirmationScreen = new SwitchApprovalConfirmationScreen(PlaywrightFactory.page);
    
        private String orgName;
        private String contactID;
        private String contactType;
        private final String submission = "CISA";
        private final String service = "3-DAY-SERVICE";
        private final String eventStatus = "Cash ISA Transfer Status Updated";
    
        @Given("{string} is logged in")
        public void isLoggedIn(String user) {
            loginPage.login(user);
        }
    
    
        @And("has the following privilege")
        public void hasTheFollowingPrivilege(DataTable dataTable) {
            List<String> listOfPrivileges = dataTable.asList();
            dashboardPage.clickToMyProfile();
            dashboardPage.clickToMyPrivileges();
            dashboardPage.clickToExtendGenericPrivileges();
            dashboardPage.clickToExtendBACS3DayLicencePrivileges();
            dashboardPage.verifyPrivilegesList(listOfPrivileges);
        }
    
    
        private void submitTransferRequest(String currYrTrans, String submission, String name, DataTable prvYrData) {
            contactID = dashboardPage.getContactId();
            contactType = dashboardPage.getContactType();
            orgName = dashboardPage.getOrgName();
            String nINumber = validateNiNumber();
            scenarioContext.set("validNI", nINumber);
            servicesPage.gotoscreen(submission);
            cisaInitiate.enterCISAInfo(submission);
            cisaInitiate.enterCustomDetails();
            cisaInitiate.populateNiNumber(nINumber);
    
            if (!name.isEmpty() && submission.equals("CISA")) {
                cisaInitiate.populateNameField(name);
                cisaInitiate.populateSurnameField(name);
            }
            cisaInitiate.enterAcqPartyDetails();
            // ... (continues)
            cisaInitiate.populateRegisteredNameField(name);
            cisaInitiate.populateRegisteredSurnameField(name);
    
            }
            cisaInitiate.submit();
        }
        }
    
    
        @When("^user submits (.*) current year (.*) transfer request$")
        public void userSubmitsTransferRequest(String currYrTrans, String submission, DataTable prvYrData) throws ParseException, InterruptedException {
            userSubmitsTransferWithSurname(currYrTrans, submission, "", prvYrData);
        }
    
    
        @When("user submits {string} current year {string} transfer request with name {string}")
        public void userSubmitsTransferWithSurname(String currYrTrans, String submission, String name, DataTable prvYrData) throws ParseException, InterruptedException {
            submitTransferRequest(currYrTrans, submission, name, prvYrData);
    
            // Click Yes on modal informing of similarity with this transfer request and others
            if (isElementVisiblePassFast(2, SIMILAR_TRANSFER_REQUEST_MODAL)) {
                clickWebElement("//button[contains(text(), 'Yes')]");
            }
            cisaSummary.verifySummaryPage();
            servicesPage.combinedClickToConfirm();
            signAndSubmit.signAndSubmitCisaInitiate();
        }
    
    
        @When("user tries to submit {string} current year {string} transfer request with name {string}")
        public void userTriesToSubmitTransferWithSurname(String currYrTrans, String submission, String name, DataTable prvYrData) {
            submitTransferRequest(currYrTrans, submission, name, prvYrData);
        }
    
    
        @Given("user verify CISA summary page")
        public void user_verify_cisa_summary_page() throws InterruptedException {
            // ... (continues)
    
}


