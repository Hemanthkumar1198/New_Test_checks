package voca.actions;
    
	import com.microsoft.playwright.Page;
	import com.microsoft.playwright.options.LoadState;
	import com.vocalink.bacs.test.exception.LoginException;
	import com.vocalink.bacs.core.spring.AppContext;
	import com.vocalink.bacs.core.config.ConfigManager;
	import com.vocalink.bacs.core.config.ConfigConstants;
	import com.vocalink.bacs.core.signature.SignatureProvider;
	import com.vocalink.bacs.core.certificate.CertificateStore;
	import com.vocalink.bacs.core.signature.impl.SignatureManager;
	import com.vocalink.bacs.core.certificate.exception.IdentityException;
	import com.vocalink.bacs.model.user.User;
	import com.vocalink.bacs.ui.BrowserException;
	import com.vocalink.bacs.util.PlaywrightFactory;
	import com.vocalink.bacs.utility.ScenarioContext;

	import lombok.SneakyThrows;
	import org.junit.Assert;
	import org.springframework.context.ApplicationContext;

	import java.net.URLDecoder;
	import java.util.Map;
	import java.util.regex.Matcher;
	import java.util.regex.Pattern;

	import static com.vocalink.bacs.core.certificate.exception.IdentityException.ExceptionDetails.CONTACT_DN;
	import static java.nio.charset.StandardCharsets.UTF_8;

	public class LoginPage extends UIBasePage {

	    private final Pattern pattern = Pattern.compile("\\d+");
	    private final ApplicationContext appContext = AppContext.getContext();
	    private final ConfigManager configManager = ConfigManager.getInstance();
	    private final SignatureProvider signatureProvider = new SignatureManager();
	    private final ScenarioContext scenarioContext = appContext.getBean(ScenarioContext.class);
	    private final CertificateStore certificateStore = appContext.getBean(CertificateStore.class);
	    private final ErrorNotificationPage errorNotificationPage = new ErrorNotificationPage(PlaywrightFactory.page);

	    public LoginPage(Page page) { super(page); }

	    /**
	     * Logs in the given contact using either autologin or certificate login depending on the autologin environment variable.
	     *
	     * @param contact Contact to log in.
	     */
	    @SneakyThrows
	    public void login(String contact) {
	        // Check user can be logged in
	        final String contactID = configManager.getUserConfig().getProperty(contact + "_ID");
	        final String contactDN = configManager.getUserConfig().getProperty(contact + "_DN");
	        if (contactID == null || contactDN == null) {
	            throw new LoginException("Unable to find \"" + contact + "\"'s contact ID and DN in sut-user.properties file");
	        }
	        final String userNumber = getUserNumberFromDN(contactDN);
	        if (userNumber.isEmpty()) {
	            throw new LoginException("Unable to find user number from contact DN: \"" + contactDN + "\" when trying to log in: \"" + contact + "\"");
	        }
	        if (!vocaSecService.isContactActive(contactID, userNumber)) {
	            throw new LoginException("Unable to Login contact: \"" + contact + "\" because their DN was not active with user number: \"" + userNumber + "\"");
	        }
	        if (!vocaSecService.isOnlyOneContact(userNumber)) {
	            throw new LoginException("Unable to Login contact: \"" + contact + "\" because other contacts are also active for user number: \"" + userNumber + "\"");
	        }
	        scenarioContext.set("page", getPage());
	        if (isElementVisible("//div[contains(text(), 'Error 404')]")) {
	            throw new LoginException("404 error encountered. Unable to load login page.");
	        }

	        // Login using appropriate method
	        if (Boolean.parseBoolean(System.getProperty("UseSoftCertificates"))) {
	            certificateLogin(contact);
	        } else {
	            autoLogin(contact);
	        }

	        if (isElementVisible("//h1[contains(text(), 'BACS Payment service unavailable')]")) {
	            throw new LoginException("BACS Payment service not available on this environment. Please try another environment or try again later.");
	        }

	        errorNotificationPage.checkLoginErrorNotification();
	        if (isElementVisible("//img[@src='error']")) {
	            throw new BrowserException.ApplicationExceptionOnPage("An error has occurred during login and the user was redirected to the old Ba");
	        }

	        scenarioContext.set("user", contact);
	    }

	    /**
	     * Uses autologin to log in the given contact
	     * @param contact Contact to log in
	     */
	    private void autoLogin(String contact) { navigateToUrl(contact); }

	    /**
	     * Uses certificates to log in the given contact.
	     * @param contact Contact to log in
	     */
	    @SneakyThrows
	    private void certificateLogin(String contact) {
	        User currentUser = getUserConfig(contact);
	        checkUserHasCertificate(currentUser);

	        // Navigate to login page first otherwise data to sign isn't present
	        navigateToLoginPage();
	        loginWithSignature(getSignature(currentUser));
	        scenarioContext.set("contactName", contact);
	    }

	    /**
	     * Create a User object based off of a given contact
	     * @param contactDN Contact the User should be based on
	     * @return New User based on contact
	     */
	    private User getUserConfig(String contactDN) {
	        String contact = configManager.getUserConfig().getProperty(contactDN);
	        if (contact == null) {
	            throw new IdentityException.ContactNotFoundException(String.format(CONTACT_DN, contactDN));
	        }
	        scenarioContext.set("currentUser", contact);
	        return new User().setDistinguishedName(contact).setAlias(contactDN);
	    }

	    /**
	     * Verify the given user has a certificate in the certificate store.
	     * @param currentUser User to check
	     */
	    private void checkUserHasCertificate(User currentUser) {
	        certificateStore.verifyContactPresentCertificateStore(currentUser.getDistinguishedName());
	    }

	    /**
	     * Navigate browser to the login page.
	     */
	    public void navigateToLoginPage() {
	        final String baseUrl = configManager.getSutConfig().getProperty(ConfigConstants.APPLICATION_BASE_URL);
	        getPage().navigate(baseUrl + "/loginBrowser.do");
	        checkBacsPaymentServiceIsAvailable();
	    }

	    /**
	     * Send the request used to perform certificate login.
	     * @param signature Signature to use for authentication
	     */
	    @SneakyThrows
	    private void loginWithSignature(String signature) {
	        final String baseUrl = configManager.getSutConfig().getProperty(ConfigConstants.APPLICATION_BASE_URL);
	        listAPIResponseUserLogin = getAPIResponse();
	        getPage().navigate(baseUrl + "/authenticatePswRefresh.do?Signature=" + signature);
	        getPage().waitForLoadState(LoadState.NETWORKIDLE);
	        checkBacsPaymentServiceIsAvailable();
	    }

	    /**
	     * Returns the value of the key from the api json response.
	     * @param key the key for which value is needed from api response
	     */
	    @SneakyThrows
	    public String getValueFromAPIResponses(String key) {
	        return getKeyValueAPIResponses(key, listAPIResponseUserLogin);
	    }

	    /**
	     * Verifies the date comparison and will fail if the date is not per expectation.
	     * @param expected is expected i.e. true/false
	     * @param DateComparison boolean value returned to see if the date is greater or less than sysdate as per the case.
	     */
	    public void compare2FADate(String expected, boolean DateComparison) {
	        checkFieldContains(expected, String.valueOf(DateComparison));
	    }

	    /**
	     * Generate the signature to be used for certificate login.
	     * @param user User to login
	     * @return A signature which can be used to log in the given user.
	     */
	    private String getSignature(User user) {
	        String dataToSign = getPage().locator("//embed").getAttribute("DataToBeSigned");
	        String urlDecodedDataToSign = URLDecoder.decode(dataToSign, UTF_8);
	        return signatureProvider.generateSignature(urlDecodedDataToSign, user.getDistinguishedName(), true);
	    }

	    /**
	     * Fails test early if the BACS Payment Service is unavailable.
	     * Does nothing otherwise.
	     */
	    private void checkBacsPaymentServiceIsAvailable() {
	        if (isElementVisible("//h1[contains(text(), 'BACS Payment service unavailable')]")) {
	            Assert.fail("BACS Payment service not available on this environment. Please try another environment or try again later.");
	        }
	    }

	    /**
	     * Extracts the user number from the given contact DN.
	     * @param contactDN Contact DN to extract the user number of
	     * @return The contact DN's user number. The empty string otherwise.
	     */
	    private String getUserNumberFromDN(final String contactDN) {
	        final Matcher matcher = pattern.matcher(contactDN);
	        for (int i = 0; i < 2; i++) {
	            if (!matcher.find()) {
	                return "";
	            }
	        }
	        return matcher.group();
	    }
    
}
