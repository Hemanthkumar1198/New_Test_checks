package voca.stepdef;

import com.vocalink.bacs.actions.Aservices.DDIC.DDICReasonCode7Page;
import com.vocalink.bacs.actions.OCS.OutputCollectionPage;
import com.vocalink.bacs.actions.PSWReports.OCReportPage;
import com.vocalink.bacs.actions.Services.ServicesPage;
import com.vocalink.bacs.core.config.ConfigManager;
import com.vocalink.bacs.util.PlaywrightFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.io.File;
import java.text.ParseException;

public class OCSStepDef {

    private static int fileCount;
    private static String downloadsPath;

    private static final Logger LOGGER = LogManager.getLogger(OCSStepDef.class);

    protected ConfigManager configManager = ConfigManager.getInstance();

    private final ServicesPage servicesPage = new ServicesPage(PlaywrightFactory.page);
    private final OCReportPage ocReportPage = new OCReportPage(PlaywrightFactory.page);
    private final OutputCollectionPage outputCollectionPage = new OutputCollectionPage(PlaywrightFactory.page);
    private final DDICReasonCode7Page ddicReasonCode7Page = new DDICReasonCode7Page(PlaywrightFactory.page);

    @When("user navigates to Output Collection page")
    public void userNavigatesToOutputCollectionPage() {
        fileCount = countFilesInDownloads();
        servicesPage.clickToServices();
        servicesPage.clickOutputCollection();
        outputCollectionPage.validateHeader();
    }

    @When("user downloads {string} output file")
    public void userDownloadsOutput(String outputType) { 
        outputCollectionPage.downloadOutputFile(outputType); 
    }

    @Then("user verifies that the download is successful")
    public void verifyThatTheDownloadIsSuccessful() {
        Assert.assertTrue("File download unsuccessful", countFilesInDownloads() > fileCount);
    }

    @When("user change the items per page as {int}")
    public void userChangeTheItemsPerPageAs(int numberOfItems) {
        outputCollectionPage.selectItemsPerPage(numberOfItems);
    }

    @Then("user verifies all the {int} items are displayed")
    public void verifyAllTheItemsAreDisplayed(int numberOfItems) {
        outputCollectionPage.verifyNumberOfItemsDisplayed(numberOfItems);
    }

    public static int countFilesInDownloads() {
        downloadsPath = System.getProperty("user.dir") + File.separator + "target\\Downloads";
        return new File(downloadsPath).listFiles().length;
    }

    @When("user select {word} {word} output files")
    public void selectMultipleFiles(String fileCount, String fileType) {
        LOGGER.info("User select multiple files");
        outputCollectionPage.selectNumberOfFiles(Integer.parseInt(fileCount), fileType);
    }

    @When("user select {int} items per page")
    public void userSelectItemsPerPageAs(int numberOfItems) { 
        outputCollectionPage.selectItemsPerPage(numberOfItems); 
    }

    @When("user clicks to download selected files")
    public void downloadSelectedFiles() {
        LOGGER.info("Download selected files");
        outputCollectionPage.downloadSelectedFiles();
    }

    @When("user select {word} {word} and {word} {word} output files")
    public void selectMultipleOutputFiles(String peFileCount, String peFileType, String meFileCount, String meFileType) {
        outputCollectionPage.selectNumberOfFiles(Integer.parseInt(peFileCount), peFileType);
        outputCollectionPage.selectNumberOfFiles(Integer.parseInt(meFileCount), meFileType);
    }

    @When("user clicks on processing date column")
    public void clickOnProcessingDateColumn() { 
        outputCollectionPage.clickToProcessingDateColumn(); 
    }

    @When("user verifies that the data is sorted in {word} order")
    public void verifyWebTableDataSorting(String sortOrder) throws ParseException {
        outputCollectionPage.verifyTableDataIsSorted(sortOrder);
    }

    @When("user request for the online collection reports")
    public void userGeneratesTheOnlineCollectionReports() {
        servicesPage.clickToServices();
        servicesPage.clickAserviceAndDDIC();
        ddicReasonCode7Page.navigateToDDICRC7Tab();
        ocReportPage.requestForOCReports();
    }

    @And("user searches for all types of reports")
    public void userSearchesAllTypesOfOnlineCollectionReports() {
        ocReportPage.clickToReportsOC();
        ocReportPage.selectAllMenuAndDateRange();
        ocReportPage.clickSearchButton();
    }

    @Then("verify that the reports are displayed on the screen")
    public void verifyThatTheDataIsDisplayedOnTheScreen() { 
        ocReportPage.verifyDataIsDisplayed(); 
    }
    
}
