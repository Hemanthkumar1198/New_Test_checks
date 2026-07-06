package voca.actions;
    
	import com.microsoft.playwright.Page;
	import com.vocalink.bacs.actions.ReferenceData.Model.SystemNoticeModel;
	import com.vocalink.bacs.actions.UIBasePage;
	import com.vocalink.bacs.test.data.generator.type.impl.util.DateUtil;
	import com.vocalink.bacs.util.DateUtils;

	import java.util.Map;

	public class CreateSystemNoticePage extends UIBasePage {

	    private final String title = "#title";
	    private final String summary = "#summary";
	    private final String contents = "#content";
	    private final String priorityUrgentCheckbox = "//*[@formcontrolname='priority']//label";
	    private final String serviceLicenceBacs3Checkbox = "//*[@formcontrolname='serviceLicenceBacs3']//label";
	    private final String recipientCheckbox = "(//*[contains(text(),'Recipients')])/parent::div//cds-checkbox//label[<placeholder>]";
	    private final String featuresCheckbox = "(//*[contains(text(),'Bacs features')])/parent::div//cds-checkbox//label[<placeholder>]";

	    private final SystemNoticeModel systemNoticeModel = new SystemNoticeModel();

	    public CreateSystemNoticePage(Page page) { super(page); }

	    public SystemNoticeModel createSystemNotice(String testDataKey) {
	        Map<String, String> testDataMap = testDataProvider.getTestDataWithHeader(testDataKey);
	        waitForUrl("**/system-notification/create");
	        waitForElementVisibility(title);
	        clickWebElement(title);
	        setSystemNoticeCSVDetailsToModel(testDataMap);
	        enterdatainfield(title, systemNoticeModel.getTitle());
	        enterdatainfield(summary, systemNoticeModel.getSummary());
	        enterdatainfield(contents, systemNoticeModel.getContents());
	        if (testDataMap.get("priority").equalsIgnoreCase("urgent")) {
	            selectPriorityUrgent();
	        }
	        selectAllRecipients();
	        selectBacs3Licence();
	        selectAllBacsFeatures();
	        String effectiveDate = bacsRefService.getParameterValue("10");
	        String nextExpiryDate = DateUtil.getNextExpiryDate(effectiveDate, DateUtils.FULL_DATE_SLASHED_2);
	        enterdatainfield("#expiryDate-input", nextExpiryDate);
	        return systemNoticeModel;
	    }

	    public void setSystemNoticeCSVDetailsToModel(Map<String, String> testDataMap) {
	        systemNoticeModel.setTitle(testDataMap.get("title"));
	        scenarioContext.set("title", testDataMap.get("title"));
	        systemNoticeModel.setSummary(testDataMap.get("summary"));
	        scenarioContext.set("summary", testDataMap.get("summary"));
	        systemNoticeModel.setContents(testDataMap.get("content"));
	    }

	    public void selectPriorityUrgent() { selectCheckbox(priorityUrgentCheckbox); }

	    public void selectAllRecipients() {
	        for (int i = 1; i < 5; i++) {
	            selectCheckbox(recipientCheckbox.replace(PLACEHOLDER, String.valueOf(i)));
	        }
	    }

	    public void selectAllBacsFeatures() {
	        for (int i = 1; i < 4; i++) {
	            selectCheckbox(featuresCheckbox.replace(PLACEHOLDER, String.valueOf(i)));
	        }
	    }

	    
	    public void selectBacs3Licence() { selectCheckbox(serviceLicenceBacs3Checkbox); }

	    public SystemNoticeModel editSystemNotice() {
	        clickWebElement(title);
	        systemNoticeModel.setTitle(getFakeName());
	        enterdatainfield(title, systemNoticeModel.getTitle());
	        systemNoticeModel.setSummary(getFakeName());
	        enterdatainfield(summary, systemNoticeModel.getSummary());
	        systemNoticeModel.setContents(getFakeName());
	        enterdatainfield(contents, systemNoticeModel.getContents());
	        return systemNoticeModel;
	    }
	}
    
}
