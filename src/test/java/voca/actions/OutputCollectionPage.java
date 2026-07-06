package voca.actions;
    
    import com.microsoft.playwright.Download;
	import com.microsoft.playwright.Page;
	import com.voca.bacs.actions.UIBasePage;
	import com.voca.bacs.util.DateUtils;
	import org.apache.logging.log4j.LogManager;
	import org.apache.logging.log4j.Logger;
	import org.junit.Assert;

	import java.nio.file.Paths;
	import java.text.ParseException;

	public class OutputCollectionPage extends UIBasePage {

	    private static final Logger LOGGER = LogManager.getLogger(OutputCollectionPage.class);

	    private final String outputData = "(//div[@class='bx--grid'])[1]";
	    private final String downloadBtn = "//button[contains(text(), 'Download')]";
	    private final String itemCount = "(//div[@class = 'bx--row-lg margin0 mb25 ptr'])";
	    private final String itemsPerPageLabel = "//label[contains(text(), 'Items per page: ')]";
	    private final String hdrOutputCollection = "//div[contains(text(), 'Output collection')]";
	    private final String processingDateColumnHeader = "//div[contains(text(),'Processing day')]";
	    private final String upArrowProcessingDate = processingDateColumnHeader + "/ibm-icon-arrow-up";
	    private final String downArrowProcessingDate = processingDateColumnHeader + "/ibm-icon-arrow-down";
	    private final String peOutputCheckbox = "(//div[contains(text(), 'PE Output')])[1]/preceding-sibling::ibm-checkbox";
	    private final String meaudOutputCheckbox = "(//div[contains(text(), 'MEAUD Output')])[1]/preceding-sibling::ibm-checkbox";
	    private final String itemsPerPageDropDown = "//label[contains(text(), 'Items per page: ')]/following-sibling::div/select";
	    private final String outputCollectionTable = "//div[@class = 'bx--row-lg margin0 mb25 ptr']{<placeholder>}//div[@class='bx--row']/div[2]";

	    public OutputCollectionPage(Page page) { super(page); }

	    public void validateHeader() {
	        Assert.assertTrue(isElementVisible(hdrOutputCollection));
	        waitForElement(outputData);
	    }

	    public void downloadOutputFile(String type) {
	        validateHeader();
	        selectOutputFile(type);
	        downloadSelectedFiles();
	    }

	    public void selectOutputFile(String type) {
	        switch (type) {
	            case "PE":
	                clickWebElement(peOutputCheckbox);
	                break;
	            case "MEAUD":
	                clickWebElement(meaudOutputCheckbox);
	                break;
	            default:
	                LOGGER.info("Invalid Output Type");
	                throw new IllegalArgumentException("Invalid Output Type");
	        }
	    }

	    public void selectItemsPerPage(int numberOfItems) {
	        waitForElement(itemsPerPageLabel);
	        selectOptionByValue(itemsPerPageDropDown, String.valueOf(numberOfItems));
	    }

	    public void verifyNumberOfItemsDisplayed(int numberOfItems) {
	        int numberOfRowsDisplayed = getPage().locator(itemCount).count();
	        Assert.assertTrue("All " + numberOfItems + " items are NOT displayed on screen.", numberOfItems >= numberOfRowsDisplayed);
	    }

	    public void selectNumberOfFiles(int fileCount, String fileType) {
	        selectItemsPerPage(48);
	        for (int i = 1; i <= fileCount; i++) {
	            if (fileType.equalsIgnoreCase("PE")) {
	                Assert.assertTrue(isElementVisible(peOutputCheckbox.replace("[1]", "[" + i + "]")));
	                clickWebElement(peOutputCheckbox.replace("[1]", "[" + i + "]"));
	            }
	            else if (fileType.equalsIgnoreCase("MEAUD")) {
	                Assert.assertTrue(isElementVisible(meaudOutputCheckbox.replace("[1]", "[" + i + "]")));
	                clickWebElement(meaudOutputCheckbox.replace("[1]", "[" + i + "]"));
	            }
	        }
	    }

	    public void downloadSelectedFiles() {
	        Download download = getPage().waitForDownload(() -> clickWebElement(downloadBtn));
	        LOGGER.debug("File downloaded from {}", download.url());
	        LOGGER.debug("File downloaded at {}", download.path());
	        String downloadFileLocation = "target/downloads/output_" + getSystemDate("yyyyMMddhhmmss") + ".zip";
	        download.saveAs(Paths.get(downloadFileLocation));
	        LOGGER.info("Downloaded file saved as {}", downloadFileLocation);
	    }

	    public void clickToProcessingDateColumn() {
	        validateHeader();
	        waitForElement(processingDateColumnHeader);
	        waitForElement(downArrowProcessingDate);
	        clickWebElement(processingDateColumnHeader);
	        waitForElement(upArrowProcessingDate);
	    }

	    public void verifyTableDataIsSorted(String sortOrder) throws ParseException {
	        validateHeader();
	        int numberOfRecordsDisplayed = getPage().locator(itemCount).count();
	        String currentStrDate;
	        String nextStrDate;
	        boolean isSorted = false;
	        for (int i = 1; i <= numberOfRecordsDisplayed; i++) {
	            if (i == numberOfRecordsDisplayed) {
	                break;
	            }
	            currentStrDate = getText(outputCollectionTable.replace(PLACEHOLDER, String.valueOf(i)));
	            nextStrDate = getText(outputCollectionTable.replace(PLACEHOLDER, String.valueOf(i + 1)));
	            LOGGER.debug("compare current {} and next {}", currentStrDate, nextStrDate);
	            if (sortOrder.toLowerCase().contains("ascending")) {
	                isSorted = DateUtils.compareDates(currentStrDate, nextStrDate, DateUtils.FULL_DATE_WITH_TEXT_MONTH_DASHED) <= 0;
	            }
	            else if (sortOrder.toLowerCase().contains("descending")) {
	                isSorted = DateUtils.compareDates(currentStrDate, nextStrDate, DateUtils.FULL_DATE_WITH_TEXT_MONTH_DASHED) >= 0;
	            }
	            checkFieldContains("true", String.valueOf(isSorted));
	        }
	    }

    
}
