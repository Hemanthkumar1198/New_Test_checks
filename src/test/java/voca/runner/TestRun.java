package voca.runner;

import com.vocalink.bacs.actions.UIBasePage;
import com.vocalink.bacs.test.environment.Environments;
import com.vocalink.bacs.utility.PropertyReader;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"com/vocalink/bacs/StepDefs"},
    features = {"src/test/resources/reports/grs/acsw-new-bank-summary-redirections-report.feature"},
    plugin = {"pretty", "html:target/cucumber.html", "json:target/cucumber.json"},
    tags = "@grs_acsw_new_bank_report"
)

public class TestRun {

    private static String environment;
    private static String autoLoginCheck;
    private static String softCertificateCheck;

    @BeforeClass
    public static void checkAutoLogins() {
        environment = (System.getProperty("env") != null) ? System.getProperty("env").toLowerCase() : new PropertyReader().getEnv();
        System.setProperty(Environments.ENVIRONMENT_CONFIG_NAME, environment);
        autoLoginCheck = (System.getProperty("autoLoginCheck") != null) ? System.getProperty("autoLoginCheck").toLowerCase() : 
            new PropertyReader().getAutoLoginCheckValue();
        softCertificateCheck = (System.getProperty("softCertificateCheck") != null) ? System.getProperty("softCertificateCheck").toLowerCase() : 
            String.valueOf(new PropertyReader().useSoftCertificates());

        if (autoLoginCheck.equalsIgnoreCase("true")) {
            UIBasePage uibasePage = new UIBasePage();
            uibasePage.checkContact(uibasePage.getAutoLogins(), environment);
        }
    }
}

    
}
