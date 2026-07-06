package voca.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.vocalink.bacs.StepDefs.AbstractStepDef;
import com.vocalink.bacs.test.environment.Environments;
import com.vocalink.bacs.ui.BrowserException;
import com.vocalink.bacs.util.DateUtils;
import com.vocalink.bacs.util.PlaywrightFactory;
import com.vocalink.bacs.utility.PropertyReader;
import lombok.SneakyThrows;
import net.minidev.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.core.ConditionTimeoutException;
import com.google.gson.*;

import org.junit.Assert;

import javax.persistence.NoResultException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.options.LoadState.*;
import static com.vocalink.bacs.ui.BrowserException.ExceptionDetails.*;
import static org.awaitility.Awaitility.waitAtMost;

public class UIBasePage extends AbstractStepDef {

    private static final String SIGN_IN_SUFFIX_PKI = "/loginBrowserAuto.do?user=";
    private static final Logger LOGGER = LogManager.getLogger(UIBasePage.class);

    protected static final String GBP = "£";
    protected static final String CONFIRM_ID = "#confirm";
    protected static final String SORT_CODE_ID = "#sortCode";
    protected static final String PLACEHOLDER = "<placeholder>";
    protected static final String REJECT_FOR_REPAIR = "Reject for repair";
    protected static final String LOGIN_ERROR_MSG = "//h2[text()='Error 404--Not Found']";
    protected static final String SUMMARY_SERVICE_URI = "/newbacs/rdserv/summary.do";
    protected static final String INPUT_AREAS = "//ibm-checkbox/input|//psw-textbox/input|//psw-dropdown//ibm-dropdown/div|//ibm-date-picker-input//input";

    public final Properties autoLoginProperties;

    protected final String nextBtn = "//button[contains(text(), 'Next')]";
    protected final String submitBtn = "//button[contains(text(), 'Submit')]";
    protected final String searchBtn = "//button[contains(text(), 'Search')]";
    protected final String rejectBtn = "//button[contains(text(), 'Reject')]|//div[contains(text(), 'Reject')]";
    protected final String deleteBtn = "//button[contains(text(), 'Delete')]";
    protected final String confirmBtn = "//button[contains(text(), 'Confirm')]";
    protected final String placeholderListTitle = "//li[@title='<placeholder>']";
    protected final String continueBtn = "//button[contains(text(), 'Continue')]";
    protected final String approvalIdLocator = "//div[contains(text(), 'approval Id')]";
    protected final String successConfirmation = "//span[contains(text(), 'Success Transaction Applied')]";
    protected final String firstSearchButton = "(//button[@class='primary cds--btn cds--btn--primary'])[1]";
    protected final String secondSearchButton = "(//button[@class='primary cds--btn cds--btn--primary'])[2]";
    protected final String firstSummaryColumn = "//*[class='cds--structured-list-row']//cds-list-column[1]";
    protected final String secondSummaryColumn = "//*[class='cds--structured-list-row']//cds-list-column[2]";
    protected final String fifthSummaryColumn = "//*[class='cds--structured-list-row']//cds-list-column[5]";
    protected final String sixthSummaryColumn = "//*[class='cds--structured-list-row']//cds-list-column[6]";
    
    public static Map<String, String> listAPIResponseUserLogin;

    private String value;
    private final String dateFormat1 = "yyyy-MM-dd hh:mm:ss";

    private final Page page;
    private final Faker faker;
    private final PlaywrightFactory playwrightFactory;

    Random rand = new Random();
    ObjectMapper objectMapper = new ObjectMapper();

    List<String> elementList;
    List<ElementHandle> listOfDropdownElements;
    Multimap<String, String> hashMap = ArrayListMultimap.create();

    public UIBasePage(Page page) {
        super();
        this.playwrightFactory = new PlaywrightFactory();
        this.autoLoginProperties = new PropertyReader().loadEnvProperty();
        this.page = PlaywrightFactory.page;
        this.faker = new Faker(new Locale("en-GB"));
    }

    public Page getPage() {
        return this.page;
    }

    public void startBrowser() {
        playwrightFactory.initBrowser();
        playwrightFactory.startTraceView();
    }

    public void closeBrowser() {
        playwrightFactory.closeBrowser();
    }

    public void stopTraceView(String scenarioName) {
        playwrightFactory.stopTraceView(scenarioName);
    }

    public void clickWebElement(String element) {
        try {
            page.locator(element).scrollIntoViewIfNeeded();
            page.waitForLoadState(DOMCONTENTLOADED);
            page.locator(element).click();
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    public void enterDataInField(String element, String value) {
        try {
            page.fill(element, value);
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_POPULATE, value), e);
        }
    }

    public String getRequestID(String id) {
        try {
            return page.locator(id).textContent().replace(".", "").trim();
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_RETURN_VALUE, id), e);
        }
    }

    public String getFieldValue(String element) {
        try {
            return page.locator(element).innerText();
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_RETURN_VALUE, element), e);
        }
    }

    public void waitForElement(String element) {
        try {
            page.waitForLoadState(DOMCONTENTLOADED);
            page.locator(element).waitFor();
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(ELEMENT_NOT_FOUND, element), e);
        }
    }

    @SneakyThrows
    public void navigateToUrl(String user) {
        listAPIResponseUserLogin = getAPIResponse();
        scenarioContext.set("User Login Time", DateUtils.getSystemDate(DateUtils.FULL_DATE_SLASHED_2) + " " + DateUtils.getCurrentTimeAMPM());
        scenarioContext.set("user", user);
        
        if (new PropertyReader().getEnv().toLowerCase().contains("tt") || new PropertyReader().getEnv().toLowerCase().contains("pt")) {
            if (user.toLowerCase().contains("direct")) {
                page.navigate(autoLoginProperties.getProperty("base_url") + SIGN_IN_SUFFIX_PKI + autoLoginProperties.getProperty(user) + "&bank=4");
            } else if (user.toLowerCase().contains("bacs")) {
                page.navigate(autoLoginProperties.getProperty("base_url") + SIGN_IN_SUFFIX_PKI + autoLoginProperties.getProperty(user) + "&bank=10");
            } else if (user.toLowerCase().contains("su")) {
                page.navigate(autoLoginProperties.getProperty("base_url") + SIGN_IN_SUFFIX_PKI + autoLoginProperties.getProperty(user) + "&bank=2");
            }
        } else if (user.contains("ASM")) {
            page.navigate(autoLoginProperties.getProperty("login_page_url"));
        } else if (page.locator(UIBasePage.LOGIN_ERROR_MSG).isVisible()) {
            page.reload();
        } else {
            if (autoLoginProperties.getProperty("base_url").contains("apache-baap") || new PropertyReader().getEnv().toLowerCase().contains("ct-12")) {
                page.navigate(autoLoginProperties.getProperty("base_url") + SIGN_IN_SUFFIX_PKI + autoLoginProperties.getProperty(user));
            } else {
                page.navigate(autoLoginProperties.getProperty("base_url") + SIGN_IN_SUFFIX_PKI + autoLoginProperties.getProperty(user) + "&bank=10");
            }
        }
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public List<String> getAutoLogins() {
        List<String> propertiesList = new ArrayList<>();
        String excludeKey = "base_url";
        for (String key : autoLoginProperties.stringPropertyNames()) {
            if (!key.equals(excludeKey)) {
                String value = autoLoginProperties.getProperty(key);
                propertiesList.add(key + "=" + value);
            }
        }
        return propertiesList;
    }

    @SneakyThrows
    public void checkContact(List<String> userList, String env) {
        for (String key : userList) {
            String[] keyValue = key.split("=");
            if (keyValue.length < 2) {
                LOGGER.error("Failed to check contact: {} because it is incomplete. Please check its definition in {}.properties file.", keyValue[0]);
                throw new Exception("Failed to check contact");
            }
            
            String contact = keyValue[0];
            String user = keyValue[1];
            String contactID = configManager.getUserConfig().getProperty(contact + "_ID");
            String contactDN = configManager.getUserConfig().getProperty(contact + "_DN");
            
            // ... (Remainder of method tracing truncated logic if applicable)
        }
    }

    public boolean isPKIActive(String contactID, String contact, String user, String env) {
        try {
            return true;
        } catch (NoResultException e) {
            writeAutologinStatus(contact, "user + \"-- Autologin not working. Contact is not present in T_SEC_CONTACT table.\"", env);
        }
        return false;
    }

    /**
     * Mask out all currently active DNs for the given user number and DN
     * so that only the given contact is active (activating it if needed).
     *
     * @param contact Name of the contact
     * @param user User number the contact should be mapped to
     * @param contactID Contact ID which should be mapped to this user number and DN
     * @param contactDN Contact DN the user should be mapped to
     * @param env Environment the mapping should exist in
     */
    public void verifyContactDN(String contact, String user, String contactID, String contactDN, String env) {
        if (vocaSecService.checkContactExist(contactID)) {
            try {
                // Mask entries which aren't for the given contact ID
                List<String> contactIDsToMask = vocaSecService.getContactIDsToMask(user, contactDN);
                for (String contactIDToMask : contactIDsToMask) {
                    // TODO Change bit in equalsIgnoreCase because what if the contact is masked with == or ---?
                    // Why do we need this check since we already know the contact ID needs to be masked?
                    if (!vocaSecService.getContactDN(user, contactIDToMask).equalsIgnoreCase(contactDN.concat("-"))) {
                        vocaSecService.updateContactDN(contactIDToMask, contactDN.concat("-"));
                    }
                }
                vocaSecService.updateContactDN(contactID, contactDN);
            } catch (NullPointerException e) {
                vocaSecService.updateContactDN(contactID, contactDN);
            }
            writeAutologinStatus(contact, "user + \"-- Autologin working\"", env);
        } else {
            writeAutologinStatus(contact, "user + \"-- contact \" + contactID + \" is not present in T_SEC_CONTACT_DN table\"", env);
        }
    }

    /**
     * Writes the auto login status of the given contact to the relevant
     * auto login properties file, updating the internal representation at
     * same time.
     * Passed environment value is used to determine the name of the file the
     * properties object is stored in.
     *
     * @param contact The key used in the properties object.
     * @param autoLoginStatus Current auto login status of the contact we want to store.
     * @param env Environment we are checking auto login statuses in.
     */
    public void writeAutologinStatus(String contact, String autoLoginStatus, String env) {
        autoLoginProperties.setProperty(contact, autoLoginStatus);
        try (FileOutputStream outputStream = new FileOutputStream("target/" + env + "_autologin.txt")) {
            autoLoginProperties.store(outputStream, "File updated on");
            outputStream.flush();
        } catch (Exception e) {
            LOGGER.info("Some issue with file writing, Please check.");
        }
    }

    public String getFakeName() {
        return faker.name().firstName();
        return value;
    }


    public String getFakeDateOfBirth() {
    return faker.date().birthday().toString();
}

/**
 * Returns fake postcode.
 */
public String getFakePostcode() {
    return faker.address().zipCode();
}

/**
 * Returns fake email.
 */
public String getFakeEmail() {
    return faker.internet().emailAddress();
}

/**
 * Returns unique identifier.
 */
public String getUniqueIdentifier() {

    SimpleDateFormat formatter =
            new SimpleDateFormat("yyyyMMddHHmmss");

    Date date = new Date();

    return formatter.format(date);
}

/**
 * Returns random account number.
 */
public String getAccountNumber() {

    StringBuilder accountNumber = new StringBuilder();

    for (int i = 0; i < 8; i++) {
        accountNumber.append(rand.nextInt(10));
    }

    return accountNumber.toString();
}

/**
 * Returns fake address.
 */
public String getHomeAddress() {

    return faker.address().streetAddress()
            + ", "
            + faker.address().city()
            + ", "
            + faker.address().state()
            + ", "
            + faker.address().zipCode();
}

/**
 * Returns current date.
 */
public String getCurrentDate() {

    return LocalDate.now().toString();
}

/**
 * Returns current timestamp.
 */
public String getCurrentTimeStamp() {

    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new Date());
}

    
}
