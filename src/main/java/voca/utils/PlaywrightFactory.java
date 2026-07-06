package voca.utils;

import java.util.Properties;
import java.util.logging.LogManager;

public class PlaywrightFactory {

    public static Page page;

    private static final int SLOW_MO_DELAY = 500;

    private static Browser browser;
    private static Playwright playwright;
    private static BrowserContext browserContext;

    public Properties properties;

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightFactory.class);

    public Page initBrowser() {
        playwright = Playwright.create();
        properties = new PropertyReader().loadConfigProperty();
        final String browserType = (System.getProperty("browser") != null) ? System.getProperty("browser").toLowerCase() : p...
        switch (browserType){
            case "chromium":
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setArgs(Arrays.asList("--start-maximized"))
                        .setHeadless(false)
                        .setSlowMo(SLOW_MO_DELAY)
                );
                break;
            case "chrome":
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setArgs(Arrays.asList("--start-maximized"))
                        .setHeadless(false)
                        .setSlowMo(SLOW_MO_DELAY)
                        .setChannel("chrome"),setHeadless(Boolean.parseBoolean((System.getProperty("headless")!=null)?System.get...
                break;
            case "firefox":
                public class PlaywrightFactory {

    public static Page page;

    private static final int SLOW_MO_DELAY = 500;

    private static Browser browser;
    private static Playwright playwright;
    private static BrowserContext browserContext;

    public Properties properties;

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightFactory.class);

    public Page initBrowser() {
        playwright = Playwright.create();
        properties = new PropertyReader().loadConfigProperty();
        final String browserType = (System.getProperty("browser") != null) ? System.getProperty("browser").toLowerCase() : p...
        switch (browserType){
            case "chromium":
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setArgs(Arrays.asList("--start-maximized"))
                        .setHeadless(false)
                        .setSlowMo(SLOW_MO_DELAY)
                );
                break;
            case "chrome":
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setArgs(Arrays.asList("--start-maximized"))
                        .setHeadless(false)
                        .setSlowMo(SLOW_MO_DELAY)
                        .setChannel("chrome"),setHeadless(Boolean.parseBoolean((System.getProperty("headless")!=null)?System.get...
                break;
            case "firefox":
                
            public class PlaywrightFactory {

    public static Page page;

    private static final int SLOW_MO_DELAY = 500;

    private static Browser browser;
    private static Playwright playwright;
    private static BrowserContext browserContext;

    public Properties properties;

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightFactory.class);

    public Page initBrowser() {
        playwright = Playwright.create();
        properties = new PropertyReader().loadConfigProperty();
        final String browserType = (System.getProperty("browser") != null) ? System.getProperty("browser").toLowerCase():
        properties.getProperty("browser").toLowerCase(); 
        switch (browserType){
            case "chromium":
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setArgs(Arrays.asList("--start-maximized"))
                        .setHeadless(false)
                        .setSlowMo(SLOW_MO_DELAY)
                );
                break;
            case "chrome":
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setArgs(Arrays.asList("--start-maximized"))
                        .setHeadless(false)
                        .setSlowMo(SLOW_MO_DELAY)
                        .setChannel("chrome"),setHeadless(Boolean.parseBoolean((System.getProperty("headless")!=null)?System.getProperty("headless").toLowerCase():
                        properties.getProperty("headless").toLowerCase())));"))
                break;
            case "firefox":
        browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(SLOW_MO_DELAY));
        break;
    case "safari":
        browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(SLOW_MO_DELAY));
        break;
    default:
        LOGGER.error("Given browser type: \"{}\" not recognised. Enter a valid browser name", browserType);
        break;
    }

    Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
    browserContext = browser.newContext(contextOptions.setViewportSize(null).setIgnoreHTTPSErrors(true));
    browserContext.clearCookies();
    browserContext.setDefaultTimeout(120000); //Time updated because find reports in processing reports is taking a long o...

    page = browserContext.newPage();

    return page;
}

public void startTraceView() {
    browserContext.tracing().start(new Tracing.StartOptions()
            .setScreenshots(true)
            .setSnapshots(true)
            .setSnapshots(true)
    );
}

public void stopTraceView(String scenarioName) {
    String baseFileName = convertSpacesToHyphens(scenarioName);
    int suffix = 0;
    Path file = Paths.get("target/TraceView/" + baseFileName + "-Trace.zip");
    while (Files.exists(file)) {
        file = Paths.get("target/TraceView/" + baseFileName + suffix + "-Trace.zip");
        suffix++;
    }
    browserContext.tracing().stop(new Tracing.StopOptions().setPath(file));
}

public void closeBrowser() { browser.close(); }

public Playwright getPlaywright() { return playwright; }

public BrowserContext getBrowserContext() { return browserContext; }

/**
 * Replaces all space characters (' ') in the given string with
 * a hyphen ('-')
 * @param str The string to operate on
 * @return A new string similar to the original string but with all
 * spaces replaced with hyphens
 */
private String convertSpacesToHyphens(String str) {
    StringBuilder newStr = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
        newStr.append(str.charAt(i) == ' ' ? '-' : str.charAt(i));
    }
    return newStr.toString();
}
    
}
