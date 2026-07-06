package voca.hooks;

import com.microsoft.playwright.Page;
import com.vocalink.bacs.test.environment.Environments;
import com.vocalink.bacs.util.PlaywrightFactory;
import com.vocalink.bacs.utility.PropertyReader;
import com.vocalink.bacs.actions.UIBasePage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

public class Hooks {

    private static final Logger LOGGER = LogManager.getLogger(Hooks.class);
    public Page page;

    private String environment;
    private boolean useSoftCertificates;
    private final PropertyReader propertyReader = new PropertyReader();

    @Before
    public void setup() {
        environment = propertyReader.getEnv();
        useSoftCertificates = propertyReader.useSoftCertificates();

        LOGGER.info("Environment: {}", environment);
        LOGGER.info("Using soft certificates for login: {}", useSoftCertificates);

        System.setProperty(Environments.ENVIRONMENT_CONFIG_NAME, environment);
        System.setProperty("UseSoftCertificates", String.valueOf(useSoftCertificates));

        UIBasePage uiBasePage = new UIBasePage(this.page);
        uiBasePage.startBrowser();
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            try {
                scenario.attach(PlaywrightFactory.page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("target/Screenshots/" + scenario.getName() + "screenshot.png"))
                    .setFullPage(true)), "image/png", "");
                new UIBasePage(this.page).stopTraceView(scenario.getName());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        new UIBasePage(this.page).closeBrowser();
        new UIBasePage(this.page).flush();
    }
    
}
