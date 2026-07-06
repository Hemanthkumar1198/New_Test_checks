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


    value = "AutoPSW" + faker.name().firstName();
        return value;
    }

    public String getFakeDateOfBirth() {
        SimpleDateFormat dt = new SimpleDateFormat(DateUtils.FULL_DATE_SLASHED_2);
        return dt.format(faker.date().birthday());
    }

    public String getFakePostalCode() {
        value = faker.address().zipCode();
        return value;
    }

    public String getfakeemail() {
        value = faker.internet().emailAddress();
        return value;
    }

    public String getUniqueIdentifier() {
        value = faker.bothify("########");
        return value;
    }

    public String getAccountNumber() {
        value = faker.bothify("########");
        return value;
    }

    public String getHomeAddress() {
        value = faker.address().fullAddress();
        return value;
    }

    public String getOrganisationName() {
        value = faker.company().name();
        return value;
    }

    public String getBranchCode() {
        value = faker.bothify("###");
        return value;
    }

    public String getFakeTelephoneDialingCode() {
        value = faker.bothify("#####");
        return value;
    }

    public String getFakeTelephoneNumber() {
        value = faker.bothify("########");
        return value;
    }

    public String getFakeSortcode() {
        value = faker.bothify("######");
        return value;
    }

    public String getFakeFundTransferLimit() {
        value = faker.bothify("###");
        return value;
    }

    public String getFakeZip() {
        value = faker.bothify("###");
        return value;
    }

    public String getCityName() {
        value = faker.address().cityName();
        return value;
    }

    public String getStreetAddress() {
        value = faker.address().streetAddress();
        return value;
    }

    public String getThresholdNumber() {
        value = String.valueOf(faker.number().numberBetween(1, 99999));
        return value;
    }

    public String getRandomInteger() { return String.valueOf(faker.number().numberBetween(1, 99)); }

    public String generateBankSortCode() {
        int startRange = Integer.parseInt(configManager.getSutConfig().getProperty("rd.sortCode.startRange"));
        int endRange = Integer.parseInt(configManager.getSutConfig().getProperty("rd.sortCode.endRange"));
        int bankOfficeSortCode = faker.number().numberBetween(startRange, endRange);
        while (!bacsRefService.isOfficeRegistered(String.valueOf(bankOfficeSortCode))) {
            bankOfficeSortCode = faker.number().numberBetween(startRange, endRange);
        }
        scenarioContext.set("bankOfficeSortCode", bankOfficeSortCode);
        return String.valueOf(bankOfficeSortCode);
    }

    public String getCycleDate() { return bacsAccswService.getSwitchDate(); }

    public String addDays(String cycleDate, String format) {
        return DateUtils.getDate(cycleDate, 1, format);
    }

    public void assertTextPresentOnPage(String value) {
        if (!page.isVisible(value)) {
            throw new BrowserException.TextNotFoundOnPageException(String.format(TEXT_VALUE, value));
        }
    }

    public List<ElementHandle> getAllElements(String element) {
        while (page.querySelectorAll(element).isEmpty()) {
        }
        return page.querySelectorAll(element);
    }

    public List<String> getListOfElementTextContent(List<ElementHandle> elementHandleList) {
        List<String> listofElementTextContent = new ArrayList<>();
        for (int i = 0; i <= elementHandleList.size() - 1; i++) {
            listofElementTextContent.add(elementHandleList.get(i).textContent().trim());
        }
        return listofElementTextContent;
    }

    public String getText(String element) { return page.locator(element).innerText(); }

    public String getTextFieldValue(String element) { return page.locator(element).inputValue(); }

    public void uploadFile(String element, Path filePath) {
        try {
            page.setInputFiles(element, filePath);
        }
        catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(FILE_NOT_FOUND, element), e);
        }
    }

    public String getSystemDate(String dateFormat) {
        SimpleDateFormat sysDate = new SimpleDateFormat(dateFormat);
        Date date = new Date();
        return sysDate.format(date.getTime());
    }

    public boolean isElementVisible(String element) {
        try {
            return page.isVisible(element);
        }
        catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(ELEMENT_NOT_VISIBLE, element), e);
        }
    }

    public void assertNavigationPane(String element, String textFromNavigationPane) {
        Assert.assertTrue(page.innerText(element).equalsIgnoreCase(textFromNavigationPane));
    }

    public void doubleClickWebElement(String element) {
        try {
            page.dblclick(element);
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    public void selectOption(String element, String option) {
        try {
            page.selectOption(element, option);
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    public boolean isElementDisplayed(String element) { return page.isVisible(element); }

    /**
     * Get the given value from the given multimap, replacing all non-breakable spaces
     * with normal spaces.
     *
     * @param value Value to get from the multimap
     * @param map Multimap to get value from
     * @return The string at the given value in the multimap if it exists. String representing null otherwise.
     */
    public String getvalue(String value, Multimap<String, ?> map) {
        value = String.valueOf(map.get(value)).replaceAll("[\\[\\]\\{\\}]", "");
        value = value.replaceAll("NBSP", " ");
        return value;
    }

    public String removeBrackets(String value) { return value.replaceAll("[\\[\\]\\{\\}]", ""); }

    public Multimap<String, String> getListOfSummaryPage(String summaryKey, String summaryValue) throws InterruptedException {
        Thread.sleep(2000);
        Multimap<String, String> mapOfSummary = ArrayListMultimap.create();
        List<ElementHandle> listOfSummaryKey = getAllElements(summaryKey);
        List<ElementHandle> listOfSummaryValue = getAllElements(summaryValue);
        for (int i = 0; i < (Math.min(listOfSummaryKey.size(), listOfSummaryValue.size())); i++) {
            mapOfSummary.put(listOfSummaryKey.get(i).innerText().trim(), listOfSummaryValue.get(i).innerText().trim());
        }
        return mapOfSummary;
    }

    public Multimap<String, String> getListOfSummaryPage(String summaryKey, String summaryValue, String rowCount) throws InterruptedException {
        Thread.sleep(2000);
        Multimap<String, String> mapOfSummary = LinkedHashMultimap.create();
        List<ElementHandle> listOfSummaryKey = getAllElements(summaryKey);
        List<ElementHandle> listOfSummaryValue = getAllElements(summaryValue);
        for (int i = 0; i <= Integer.parseInt(rowCount); i++) {
            if (i == Integer.parseInt(rowCount)) {
                mapOfSummary.put(listOfSummaryKey.get(i).innerText().trim(), listOfSummaryValue.get(i).innerText().trim());
            } else {
                mapOfSummary.put(listOfSummaryKey.get(i).innerText().trim(), listOfSummaryValue.get(i).innerText().trim());
            }
        }
        return mapOfSummary;
    }

    public Map<String, String> getList(String summaryKey, String summaryValue) {
        isElementVisiblePassFast(4, summaryKey);
        isElementVisiblePassFast(4, summaryValue);
        Map<String, String> mapOfSummary = new HashMap<>();
        List<ElementHandle> listOfSummaryKey = getAllElements(summaryKey);
        List<ElementHandle> listOfSummaryValue = getAllElements(summaryValue);
        for (int i = 0; i < listOfSummaryKey.size(); i++) {
            mapOfSummary.put(listOfSummaryKey.get(i).innerText().trim(), listOfSummaryValue.get(i).innerText().trim());
        }
        return mapOfSummary;
    }

    public void checkassertion(String expected, String actual) { Assert.assertEquals(expected, actual); }

    public void checkFieldContains(String field1, String field2) { Assert.assertTrue(field1.contains(field2)); }

    public void checkFieldContains(Collection<String> field1, String field2) {
        Assert.assertTrue(field1.contains(field2));
    }

    public void checkValueNotPresetInList(String value, List<String> list) { Assert.assertFalse(list.contains(value)); }

    public void checkURLAssertion(String text) { Assert.assertTrue(page.url().contains(text)); }

    public int getSumOfListItems(List<String> amt) {
        List<Integer> intList = amt.stream().map(Integer::parseInt).collect(Collectors.toList());
        return intList.stream().mapToInt(Integer::intValue).sum();
    }

    public String getTextInLowerCase(String value) { return value.toLowerCase().replaceAll("[\\[\\]\\{\\}]", ""); }

    public void flush() { scenarioContext.flush(); }

    public void checkNotNullAssertion(String field) { Assert.assertNotNull(field); }

    public boolean isElementEnabled(String element) { return page.isEnabled(element); }

    public String getSwitchDate(String newFormat, String urn) {
        return DateUtils.changeDateFormat(bacsAccswService.getEffectiveACSWSwitchDate(urn), dateFormat1, newFormat);
    }

    public String getReqRedirectionDate(String urn, String newFormat) {
        return DateUtils.changeDateFormat(bacsAccswService.getReqRedirectionDate(urn), dateFormat1, newFormat);
    }

    public void updateBalanceTransferDate(String date, String urn) {
        bacsAccswService.setAccountSwitchBalanceTransRequestDeadlineDate(date, urn);
    }

    public String getRandomDropdownValue(String locator, String optionsList) {
        elementList = new ArrayList<>();
        listOfDropdownElements = getAllElements(locator);
        for (int i = 1; i < listOfDropdownElements.size(); i++) {
            int x = rand.nextInt(elementList.size());
            clickWebElement(elementList.get(x));
            String selectedOption = listOfDropdownElements.get(x + 1).innerText();
            LOGGER.info("Selected Dropdown Option: {}", selectedOption);
            return selectedOption;
        }
    }

    public String getFutureDate() {
        LocalDate futureDate = LocalDate.now().plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtils.FULL_DATE_SLASHED_2);
        return futureDate.format(formatter);
    }

    public void waitForElementVisibility(String element) {
        try {
            page.locator(element).isVisible();
        }
        catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(ELEMENT_NOT_VISIBLE, element), e);
        }
    }

    /**
     * Creates a lambda which checks if the given element is visible when called.
     *
     * @param elem Element to check visibility of
     * @return True if element is visible. False otherwise.
     */
    public Callable<Boolean> elemWaitCallableFactory(String elem) { return () -> page.isVisible(elem); }

    /**
     * Waits at most timeout seconds to see if an element is visible.
     * Will return early if the element is visible before the timeout.
     *
     * @param timeout The upper bound on the number of seconds to wait for the element to be visible.
     * @param elem Element to check visibility of
     * @return True if element is visible after at most timeout seconds. False otherwise.
     */
    public boolean isElementVisiblePassFast(int timeout, String elem) {
        try {
            waitAtMost(timeout, TimeUnit.SECONDS).until(elemWaitCallableFactory(elem));
            return true;
        }
        catch (ConditionTimeoutException e) {
            return false;
        }
    }

    public void waitForElementToInvisible(String element) {
        page.locator(element).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
    }

    public void selectOptionByValue(String element, String value) {
        try {
            Locator selectElement = page.locator(element);
            selectElement.selectOption(new SelectOption().setValue(value));
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    public void selectOptionByIndex(String element, int index) {
        try {
            Locator selectElement = page.locator(element);
            selectElement.selectOption(new SelectOption().setIndex(index));
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    package com.vocalink.bacs.actions;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UIBasePage extends AbstractStepDef {
    private static final Logger LOGGER = LoggerFactory.getLogger(UIBasePage.class);
    private String value;
    private List<ElementHandle> listOfDropdownElements;
    private List<String> elementList;
    private Random rand = new Random();
    private Page page; // Derived from Playwright framework setup
    
    // Placeholder constants referenced in original catch blocks
    private static final String TEXT_VALUE = "Text value %s not found";
    private static final String FILE_NOT_FOUND = "File not found for element %s";
    private static final String ELEMENT_NOT_VISIBLE = "Element %s is not visible";
    private static final String COULD_NOT_CLICK = "Could not click element %s";
    private static final String dateFormat1 = "dd/MM/yyyy";

    // --- Image Batch 1 (1000044126, d9863d3c, 1000044128) ---

    public String getFakeFirstName() {
        value = "AutoPSW" + faker.name().firstName();
        return value;
    }

    public String getFakeDateOfBirth() {
        SimpleDateFormat dt = new SimpleDateFormat(DateUtils.FULL_DATE_SLASHED_2);
        return dt.format(faker.date().birthday());
    }

    public String getFakePostalCode() {
        value = faker.address().zipCode();
        return value;
    }

    public String getfakeemail() {
        value = faker.internet().emailAddress();
        return value;
    }

    public String getUniqueIdentifier() {
        value = faker.bothify("########");
        return value;
    }

    public String getAccountNumber() {
        value = faker.bothify("########");
        return value;
    }

    public String getHomeAddress() {
        value = faker.address().fullAddress();
        return value;
    }

    public String getOrganisationName() {
        value = faker.company().name();
        return value;
    }

    public String getBranchCode() {
        value = faker.bothify("###");
        return value;
    }

    public String getFakeTelephoneDialingCode() {
        value = faker.bothify("#####");
        return value;
    }

    public String getFakeTelephoneNumber() {
        value = faker.bothify("########");
        return value;
    }

    public String getFakeSortcode() {
        value = faker.bothify("######");
        return value;
    }

    public String getFakeFundTransferLimit() {
        value = faker.bothify("###");
        return value;
    }

    public String getFakeZip() {
        value = faker.bothify("###");
        return value;
    }

    // --- Image Batch 2 (1000044129 - 1000044133) ---

    public String getCityName() {
        value = faker.address().cityName();
        return value;
    }

    public String getStreetAddress() {
        value = faker.address().streetAddress();
        return value;
    }

    public String getThresholdNumber() {
        value = String.valueOf(faker.number().numberBetween(1, 99999));
        return value;
    }

    public String getRandomInteger() { 
        return String.valueOf(faker.number().numberBetween(1, 99)); 
    }

    public String generateBankSortCode() {
        int startRange = Integer.parseInt(configManager.getSutConfig().getProperty("rd.sortCode.startRange"));
        int endRange = Integer.parseInt(configManager.getSutConfig().getProperty("rd.sortCode.endRange"));
        int bankOfficeSortCode = faker.number().numberBetween(startRange, endRange);
        while (!bacsRefService.isOfficeRegistered(String.valueOf(bankOfficeSortCode))) {
            bankOfficeSortCode = faker.number().numberBetween(startRange, endRange);
        }
        scenarioContext.set("bankOfficeSortCode", bankOfficeSortCode);
        return String.valueOf(bankOfficeSortCode);
    }

    public String getCycleDate() { 
        return bacsAccswService.getSwitchDate(); 
    }

    public String addDays(String cycleDate, String format) {
        return DateUtils.getDate(cycleDate, 1, format);
    }

    public void assertTextPresentOnPage(String value) {
        if (!page.isVisible(value)) {
            throw new BrowserException.TextNotFoundOnPageException(String.format(TEXT_VALUE, value));
        }
    }

    public List<ElementHandle> getAllElements(String element) {
        while (page.querySelectorAll(element).isEmpty()) {
        }
        return page.querySelectorAll(element);
    }

    public List<String> getListOfElementTextContent(List<ElementHandle> elementHandleList) {
        List<String> listofElementTextContent = new ArrayList<>();
        for (int i = 0; i <= elementHandleList.size() - 1; i++) {
            listofElementTextContent.add(elementHandleList.get(i).textContent().trim());
        }
        return listofElementTextContent;
    }

    public String getText(String element) { 
        return page.locator(element).innerText(); 
    }

    public String getTextFieldValue(String element) { 
        return page.locator(element).inputValue(); 
    }

    public void uploadFile(String element, Path filePath) {
        try {
            page.setInputFiles(element, filePath);
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(FILE_NOT_FOUND, element), e);
        }
    }

    public String getSystemDate(String dateFormat) {
        SimpleDateFormat sysDate = new SimpleDateFormat(dateFormat);
        Date date = new Date();
        return sysDate.format(date.getTime());
    }

    public boolean isElementVisible(String element) {
        try {
            return page.isVisible(element);
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(ELEMENT_NOT_VISIBLE, element), e);
        }
    }

    public void assertNavigationPane(String element, String textFromNavigationPane) {
        Assert.assertTrue(page.innerText(element).equalsIgnoreCase(textFromNavigationPane));
    }

    public void doubleClickWebElement(String element) {
        try {
            page.dblclick(element);
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    public void selectOption(String element, String option) {
        try {
            page.selectOption(element, option);
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    // --- Image Batch 3 (1000044135 - 1000044143) ---

    public boolean isElementDisplayed(String element) { 
        return page.isVisible(element); 
    }

    /**
     * Get the given value from the given multimap, replacing all non-breakable spaces
     * with normal spaces.
     *
     * @param value Value to get from the multimap
     * @param map Multimap to get value from
     * @return The string at the given value in the multimap if it exists. String representing null otherwise.
     */
    public String getvalue(String value, Multimap<String, ?> map) {
        value = String.valueOf(map.get(value)).replaceAll("[\\[\\]\\{\\}]", "");
        value = value.replaceAll("NBSP", " ");
        return value;
    }

    public String removeBrackets(String value) { 
        return value.replaceAll("[\\[\\]\\{\\}]", ""); 
    }

    public Multimap<String, String> getListOfSummaryPage(String summaryKey, String summaryValue) throws InterruptedException {
        Thread.sleep(2000);
        Multimap<String, String> mapOfSummary = ArrayListMultimap.create();
        List<ElementHandle> listOfSummaryKey = getAllElements(summaryKey);
        List<ElementHandle> listOfSummaryValue = getAllElements(summaryValue);
        for (int i = 0; i < (Math.min(listOfSummaryKey.size(), listOfSummaryValue.size())); i++) {
            mapOfSummary.put(listOfSummaryKey.get(i).innerText().trim(), listOfSummaryValue.get(i).innerText().trim());
        }
        return mapOfSummary;
    }

    public Multimap<String, String> getListOfSummaryPage(String summaryKey, String summaryValue, String rowCount) throws InterruptedException {
        Thread.sleep(2000);
        Multimap<String, String> mapOfSummary = LinkedHashMultimap.create();
        List<ElementHandle> listOfSummaryKey = getAllElements(summaryKey);
        List<ElementHandle> listOfSummaryValue = getAllElements(summaryValue);
        for (int i = 0; i <= Integer.parseInt(rowCount); i++) {
            if (i == Integer.parseInt(rowCount)) {
                mapOfSummary.put(listOfSummaryKey.get(i).innerText().trim(), listOfSummaryValue.get(i).innerText().trim());
            } else {
                mapOfSummary.put(listOfSummaryKey.get(i).innerText().trim(), listOfSummaryValue.get(i).innerText().trim());
            }
        }
        return mapOfSummary;
    }

    public Map<String, String> getList(String summaryKey, String summaryValue) {
        isElementVisiblePassFast(4, summaryKey);
        isElementVisiblePassFast(4, summaryValue);
        Map<String, String> mapOfSummary = new HashMap<>();
        List<ElementHandle> listOfSummaryKey = getAllElements(summaryKey);
        List<ElementHandle> listOfSummaryValue = getAllElements(summaryValue);
        for (int i = 0; i < listOfSummaryKey.size(); i++) {
            mapOfSummary.put(listOfSummaryKey.get(i).innerText().trim(), listOfSummaryValue.get(i).innerText().trim());
        }
        return mapOfSummary;
    }

    public void checkassertion(String expected, String actual) { 
        Assert.assertEquals(expected, actual); 
    }

    public void checkFieldContains(String field1, String field2) { 
        Assert.assertTrue(field1.contains(field2)); 
    }

    public void checkFieldContains(Collection<String> field1, String field2) {
        Assert.assertTrue(field1.contains(field2));
    }

    public void checkValueNotPresetInList(String value, List<String> list) { 
        Assert.assertFalse(list.contains(value)); 
    }

    public void checkURLAssertion(String text) { 
        Assert.assertTrue(page.url().contains(text)); 
    }

    public int getSumOfListItems(List<String> amt) {
        List<Integer> intList = amt.stream().map(Integer::parseInt).collect(Collectors.toList());
        return intList.stream().mapToInt(Integer::intValue).sum();
    }

    public String getTextInLowerCase(String value) { 
        return value.toLowerCase().replaceAll("[\\[\\]\\{\\}]", ""); 
    }

    public void flush() { 
        scenarioContext.flush(); 
    }

    public void checkNotNullAssertion(String field) { 
        Assert.assertNotNull(field); 
    }

    public boolean isElementEnabled(String element) { 
        return page.isEnabled(element); 
    }

    public String getSwitchDate(String newFormat, String urn) {
        return DateUtils.changeDateFormat(bacsAccswService.getEffectiveACSWSwitchDate(urn), dateFormat1, newFormat);
    }

    public String getReqRedirectionDate(String urn, String newFormat) {
        return DateUtils.changeDateFormat(bacsAccswService.getReqRedirectionDate(urn), dateFormat1, newFormat);
    }

    public void updateBalanceTransferDate(String date, String urn) {
        bacsAccswService.setAccountSwitchBalanceTransRequestDeadlineDate(date, urn);
    }

    public String getRandomDropdownValue(String locator, String optionsList) {
        elementList = new ArrayList<>();
        listOfDropdownElements = getAllElements(locator);
        for (int i = 1; i < listOfDropdownElements.size(); i++) {
            int x = rand.nextInt(elementList.size());
            clickWebElement(elementList.get(x));
            String selectedOption = listOfDropdownElements.get(x + 1).innerText();
            LOGGER.info("Selected Dropdown Option: {}", selectedOption);
            return selectedOption;
        }
        return "";
    }

    public String getFutureDate() {
        LocalDate futureDate = LocalDate.now().plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtils.FULL_DATE_SLASHED_2);
        return futureDate.format(formatter);
    }

    public void waitForElementVisibility(String element) {
        try {
            page.locator(element).isVisible();
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(ELEMENT_NOT_VISIBLE, element), e);
        }
    }

    /**
     * Creates a lambda which checks if the given element is visible when called.
     *
     * @param elem Element to check visibility of
     * @return True if element is visible. False otherwise.
     */
    public Callable<Boolean> elemWaitCallableFactory(String elem) { 
        return () -> page.isVisible(elem); 
    }

    /**
     * Waits at most timeout seconds to see if an element is visible.
     * Will return early if the element is visible before the timeout.
     *
     * @param timeout The upper bound on the number of seconds to wait for the element to be visible.
     * @param elem Element to check visibility of
     * @return True if element is visible after at most timeout seconds. False otherwise.
     */
    public boolean isElementVisiblePassFast(int timeout, String elem) {
        try {
            waitAtMost(timeout, TimeUnit.SECONDS).until(elemWaitCallableFactory(elem));
            return true;
        } catch (ConditionTimeoutException e) {
            return false;
        }
    }

    public void waitForElementToInvisible(String element) {
        page.locator(element).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
    }

    public void selectOptionByValue(String element, String value) {
        try {
            Locator selectElement = page.locator(element);
            selectElement.selectOption(new SelectOption().setValue(value));
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    public void selectOptionByIndex(String element, int index) {
        try {
            Locator selectElement = page.locator(element);
            selectElement.selectOption(new SelectOption().setIndex(index));
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(COULD_NOT_CLICK, element), e);
        }
    }

    // --- Image Batch 4 (1000044145 - 1000044154) ---

    public String downloadFile(String element, String filePath) {
        Download download = page.waitForDownload(() -> {
            page.locator(element).click();
        });
        download.saveAs(Paths.get(filePath));
        return filePath;
    }

    public String getDateFromCalendar(String element) {
        try {
            return page.locator(element).getAttribute("aria-label");
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(ELEMENT_NOT_VISIBLE, element), e);
        }
    }

    public void scrollDown() { 
        page.keyboard().down("End"); 
    }

    public List<String> getListOfItems(String bankList) {
        List<ElementHandle> mapOfSummary = getAllElements(bankList);
        return getListOfElementTextContent(mapOfSummary);
    }

    public void clearText(String element) { 
        page.locator(element).clear(); 
    }

    public void clickElementHasTitle(String title) { 
        page.getByTitle(title).click(); 
    }

    public int getElementCount(String element) { 
        return (int) page.locator(element).count(); 
    }

    @SneakyThrows
    public String convertDateToFormat(String dateValue, String targetFormat, String originalFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(originalFormat);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(targetFormat);
        Date date = simpleDateFormat.parse(dateValue);
        return simpleDateFormat1.format(date);
    }

    public Map<String, String> convertObjectToMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    @SneakyThrows
    public Multimap<String, String> fetchSummaryPageResponse(String uri, String locator) {
        waitForElement(locator);
        Response response = getPage().waitForResponse(r -> r.url().contains(uri), () -> {
            getPage().locator(locator).click();
        });
        JSONArray jsonArray = JsonPath.parse(response.text()).read("$.bo");
        for (int i = 0; i <= jsonArray.size() - 1; i++) {
            JSONArray jsonArray1 = JsonPath.parse(response.text()).read("$.bo[" + i + "].field");
            if (jsonArray1 == null) {
                continue;
            }
            for (int j = 0; j <= jsonArray1.size() - 1; j++) {
                hashMap.put(JsonPath.parse(response.text()).read("$.bo[" + i + "].field[" + j + "].name").toString().trim().replaceAll("[^a-zA-Z0-9]", ""));
            }
        }
        return hashMap;
    }

    public void selectCheckbox(String element) { 
        page.check(element); 
    }

    public String getServiceUserNumber() {
        value = String.valueOf(faker.number().numberBetween(000001, 999999));
        return value;
    }

    public String getBureauNumber() {
        value = faker.bothify("B#####");
        return value;
    }

    public void checkMenuItemNetworkLogs() {
        try {
            List<String> urlList = new ArrayList<>();
            getPage().onResponse(response -> {
                if (!response.request().url().isEmpty()) {
                    urlList.add(response.request().url());
                }
            });
            getPage().offResponse(response -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitForMenuItemNetworkLogs() {
        try {
            getPage().waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (Exception e) {
            throw new BrowserException.NetworkLogException(e);
        }
    }

    public List<String> checkNetworkLogs(String uri) {
        List<String> urlList = new ArrayList<>();
        try {
            getPage().onResponse(response -> {
                waitForMenuItemNetworkLogs();
                if (response.request().url().contains(uri) && response.status() == 200) {
                    urlList.add(response.request().url());
                }
            });
            getPage().offResponse(response -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlList;
    }

    @SneakyThrows
    public String getKeyValueAPIResponses(String key, Map<String, String> list) {
        String value = null;
        for (Map.Entry<String, String> obj : list.entrySet()) {
            if (obj.getKey().equals(key)) {
                value = obj.getValue();
            }
        }
        return value;
    }

    /**
     * Returns the value of seed obtained from generateQR api during 2FA registration.
     * This means they both have the same number of objects and all objects are
     * equal as defined by the passed comparator.
     *
     * @param uri This is the generateQR URI sent by the calling function.
     * @param link The link or button which is pressed on URI triggering the generateQR URI. This can either be sign-in button or Enable 2FA link.
     */
    public String getSeedValue(String uri, String link) {
        String seedValue = null;
        try {
            Response response = getPage().waitForResponse(
                response1 -> response1.url().contains(uri) && response1.status() == 200,
                () -> getPage().click(link)
            );
            String jsonResponse = response.text();
            JsonObject obj = JsonParser.parseString(jsonResponse).getAsJsonObject();
            seedValue = obj.getAsJsonObject("data").get("seed").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return seedValue;
    }

    public class UIBasePage extends AbstractStepDef {

    /**
     * Returns the Map of all api responses received when the user logs in.
     *
     */
    public Map<String, String> getAPIResponse() {
        Map<String, String> hashMap = new HashMap<>();
        getPage().onResponse( Response response -> {
            try {
                if (response.status() == 200 && response.headers().get("content-type") != null && response.headers().get("content-type").contains("application/json")) {
                    String body = "";
                    body = response.text();
                    if (body.isEmpty() || body.equals("[]") || body.equals("{}") || !body.startsWith("{")) {
                        return;
                    }
                    JsonElement jsonElement = JsonParser.parseString(body);
                    if (jsonElement.isJsonObject())
                    {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
                        for (Map.Entry<String, JsonElement> entry : entries)
                        {
                            String key = entry.getKey();
                            JsonElement valueElement = entry.getValue();
                            if (valueElement.isJsonPrimitive())
                            {
                                String value = valueElement.getAsString();
                                hashMap.put(key, value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Something wrong with the API, " + response.url());
            }
        });
        return hashMap;
    }

    public List<String> checkLogs204StatusCode(String uri) {
        List<String> urlList = new ArrayList<>();
        try {
            getPage().onResponse( Response response -> {
                waitForMenuItemNetworkLogs();
                if (response.request().url().contains(uri) && response.status() == 204) {
                    urlList.add(response.request().url());
                }
            });
            getPage().offResponse( Response response -> {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlList;
    }

    /**
     * Wait until the given URL list stops being empty.
     *
     * @param urlList List of URLs to wait on.
     */
    public void waitForNetworkLogs(List<String> urlList) {
        try {
            getPage().waitForCondition(() -> !urlList.isEmpty());
        } catch (Exception e) {
            throw new BrowserException.NetworkLogException(e);
        }
    }

    public void moveMouse() { getPage().mouse().move(1, 1); }

    public void waitforurl(String url) {
        try {
            page.waitForURL(url);
        } catch (Exception e) {
            throw new BrowserException.ElementInteractionException(String.format(URL_NOT_VISIBLE, url), e);
        }
    }

    public Boolean checkWeekEndCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SUNDAY || day == Calendar.SATURDAY;
    }

    public String fetchReferralDetailResponse(String uri, Locator locator) {
        Response response = getPage().waitForResponse( Response r -> r.url().contains(uri), locator::click);
        return response.text();
    }

    public String getRandomDecimal(String decimalNumber) {
        value = faker.regexify(decimalNumber);
        return value;
    }

    public void areAllElementsPresent(List<Locator> listOfElements) {
        for(Locator locator: listOfElements) {
            assertThat(locator).isVisible();
            assertThat(locator).isEnabled();
        }
    }

    public List<Locator> convertToListOfLocators(List<String> listOfElements) {
        List<Locator> listOfLocators = new ArrayList<>();
        for (String element: listOfElements) {
            listOfLocators.add(getPage().locator(element));
        }
        return listOfLocators;
    }

    public void verifyListContent(String locator, List<String> listOfText) {
        Assert.assertTrue(listOfText.containsAll(Arrays.asList(getText(locator).split("\n"))));
    }

    public void navigateToLoginUrl() { page.navigate(autoLoginProperties.getProperty("login_page_url")); }

    public void verifyDropdownContent(String locator, List<String> listOfText) {
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) { return o1.compareTo(o2); }
        };
        Assert.assertTrue(compareLists(getListOfItems(locator), listOfText, comparator));
    }

    /**
     * Compares two lists to check if they are identical.
     * This means they both have the same number of objects and all objects are
     * equal as defined by the passed comparator.
     *
     * @param list1 The first list to compare against the second list.
     * @param list2 The second list to compare against the first list
     * @param comparator Comparator which defines the relation between two objects
     * @return True if the two lists have the same number of items and all items are equal. False otherwise.
     */
    public boolean compareLists(List<String> list1, List<String> list2, Comparator<? super String> comparator) {
        if (list1.size() != list2.size()) {
            return false;
        }

        List<String> copy1 = new ArrayList<>(list1);
        List<String> copy2 = new ArrayList<>(list2);

        Collections.sort(copy1, comparator);
        Collections.sort(copy2, comparator);

        Iterator<String> it1 = copy1.iterator();
        Iterator<String> it2 = copy2.iterator();
        while (it1.hasNext()) {
            String t1 = it1.next();
            String t2 = it2.next();
            if (comparator.compare(t1, t2) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the IDs of all currently visible elements which are matched
     * by the given locator
     *
     * @param locator The locator to use to look for elements
     * @return A list of all the IDs of the elements found using the given locator.
     */
    public List<String> getAllWebElementsPresent(String locator) {
        List<String> listOfElements = new ArrayList<>();
        Locator elements = page.locator(locator).filter(new Locator.FilterOptions().setHasText(""));
        List<ElementHandle> visibleElements = elements.elementHandles();
        for(ElementHandle element: visibleElements) {
            listOfElements.add(element.getAttribute("id"));
        }
        return listOfElements;
    }

    public String getApprovalID() {
        if (isElementVisible(approvalIdLocator)) {
            String approvalID = getText(approvalIdLocator).replaceAll("[^0-9]", "");
            scenarioContext.set("Approval ID", approvalID);
            return approvalID;
        }
        return null;
    }

    public static String generateRandomNIN() {
        String letters = "ABCEGHJKLMNPRSTWXYZ";
        Random random = new Random();

        // Generate the first two letters
        StringBuilder sb = new StringBuilder();
        sb.append(letters.charAt(random.nextInt(letters.length())));
        sb.append(letters.charAt(random.nextInt(letters.length())));

        // Generate the six digits
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }

        // Generate the final letter
        sb.append(letters.charAt(random.nextInt(letters.length())));

        return sb.toString();
    }

    /**
     * Returns a valid NI number according to the UK NI number standard regex
     * @return A valid UK NI
     */
    public String validateNiNumber() {
        String nin = generateRandomNIN();
        System.out.println("Random UK National Insurance Number: " + nin);

        // Define the regex pattern for UK National Insurance number
        String pattern = "^(?!BG)(?!GB)(?!NK)(?!KN)(?!TN)(?!NT)(?!ZZ)[A-Z]*[^DFIQUV][A-Z]*[^DFIOQUV][0-9]{6}[A-D]$";

        // Check if the generated NIN matches the pattern
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(nin);

        if (matcher.matches()) {
            System.out.println("The generated NIN matches the regex pattern: " + nin);
        }
        else {
            System.out.println("The generated NIN does not match the regex pattern: " + nin);
            return validateNiNumber();
        }
        return nin;
    }

    /**
     * Wait for the loading spinner to disappear from the page.
     */
    public void waitForLoadingSpinnerToGo() { waitForElementToInvisible("//psw-spinner"); }
}



    
}
