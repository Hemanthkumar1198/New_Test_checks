package voca.actions;

import com.vocalink.bacs.actions.UIBasePage;
import com.vocalink.bacs.util.DateUtils;
import com.vocalink.bacs.util.StringUtils;
import org.junit.Assert;


import java.util.List;


public class ProcessingReportsPage extends UIBasePage {

    private String date;
    public  String value;
    private String requestId;
    private String reportTypeId;

    private String suNumber = "#serviceUsers";
    private String dateformatFrom = "yyyy-MM-dd";
    private String report = "//div[@id='reportType']";
    private String searchURI = "Range=0&isDefault=false";
    private String startDateRadioButton = "//*[@id='label-radio-1']";
    private String viewReportURI = "/update/contactaccessedreport.do?";
    private String endDateField ="//input[@id='fileProcessingDayEnd']";
    private String startDateField = "//input[@id='fileProcessingDayStart']";
    private String serviceUserMenu = "//*[@placeholder='Enter/select service users']";
    private String suMenuItem = "(//ul[@class='bx--list-box__menu bx--multi-select']/li)[1]";
    private String selectReport = "//*[@class='bx--list-box__menu-item' and @title='<placeholder>']";
    private String viewButton = "//div[contains(text(),'<placeholder>')]/../../../..//psw-tooltip-view//ibm-tooltip-icon";
    private String downloadButton = "//div[contains(text(),'<placeholder>')]/../../../..//psw-tooltip-download//ibm-tooltip-icon";

    public void enterSearchDetails(String reportType) {
        switch (reportType) {
            case "Live Input Report";
                reportTypeId = configManager.getSutConfig().getProperty("pem.processingReport.reportTypeId");
                if (isElementVisible(suNumber)) {
                    enterdatainfield(suNumber, configManager.getSutConfig().getProperty("pem.processingReport.suNumber"));
                }
                date = DateUtils.changeDateFormat(String.valueOf(bacsRepService.getReportRequestByTypeDefId(reportTypeId).getProcessingDate()),dateformatFrom, [...]);
                break;
            case "Account Switching Case History Report":
                reportTypeId = configManager.getSutConfig().getProperty("acsw.caseHistory.reportTypeId");
                enterdatainfield(serviceUserMenu, configManager.getSutConfig().getProperty("acsw.request.case.history.sun"));
                clickWebElement(suMenuItem);
                date = DateUtils.getSystemDate(DateUtils.FULL_DATE_SLASHED_2);
                break;
            case "Cash ISA Case History Report":
                reportTypeId = configManager.getSutConfig().getProperty("cisa.caseHistory.reportTypeId");
                enterdatainfield(serviceUserMenu, configManager.getSutConfig().getProperty("cisa.request.case.history.sun"));
                clickWebElement(suMenuItem);
                date = DateUtils.getSystemDate(DateUtils.FULL_DATE_SLASHED_2);
                break;
            case "Cash ISA File Upload Result Report":
                reportTypeId = configManager.getSutConfig().getProperty("cisa.file.upload.reportTypeId");
                enterdatainfield(serviceUserMenu, configManager.getSutConfig().getProperty("cisa_su_number"));
                clickWebElement(suMenuItem);
                date = DateUtils.getSystemDate(DateUtils.FULL_DATE_SLASHED_2);
                break;
            default:
                Assert.fail("Unknown report type provided: \"" + reportType + "\" when entering search details");
        }
        clickWebElement(report);
        clickWebElement(selectReport.replace(PLACEHOLDER, reportType));
        clickWebElement(startDateRadioButton);
        enterdatainfield(startDateField, date);
        enterdatainfield(endDateField, date);
        clickWebElement(startDateRadioButton);
    }

    public void clickSearch() {
        List<String> urlList = checkNetworkLogs(searchURI);
        clickWebElement(searchBtn);
        waitForNetworkLogs(urlList);
    }

    public void viewReport(List <String> elementList) {
        requestId = bacsRepService.getReportRequestByTypeDefId(reportTypeId).getReportRequestId();
        value = StringUtils.selectStringOption(elementList);
        clickWebElement(viewButton.replace(PLACEHOLDER, requestId));
        List<String> urlList  = checkLogs204StatusCode(viewReportURI);
        clickWebElement("//*[contains(text()," + value + ")]");
        waitForNetworkLogs(urlList);
    }

    public void downloadReport(List <String> elementList) {
        requestId = bacsRepService.getReportRequestByTypeDefId(reportTypeId).getReportRequestId();
        value = StringUtils.selectStringOption(elementList);
        clickWebElement(downloadButton.replace(PLACEHOLDER, requestId));
        List<String> urlList  = checkLogs204StatusCode(viewReportURI);
        clickWebElement("//*[contains(text()," + value + ")]");
        waitForNetworkLogs(urlList);
    }

    public void verifyDatabase(String userType, String contactId) {
        switch (userType) {
            case "DirectParticipant":
                checkassertion("1", bacsRepService.getReportAccessedDetails(requestId).getReportRequestAccessedBySMValue());
                checkassertion(DateUtils.getSystemDate(DateUtils.FULL_DATE_SLASHED_2), DateUtils.changeDateFormat(bacsRepService.getReportAccessedDetails(requestId).getRe...));
                checkassertion(contactId, bacsRepService.getReportAccessedDetails(requestId).getReportRequestAccessedByContactSM());
                break;
            case "ServiceUser":
                checkassertion("1", bacsRepService.getReportAccessedDetails(requestId).getAccessedBySU());
                checkassertion(DateUtils.getSystemDate(DateUtils.FULL_DATE_SLASHED_2), DateUtils.changeDateFormat(bacsRepService.getReportAccessedDetails(requestId).getRe...));
                checkassertion(contactId, bacsRepService.getReportAccessedDetails(requestId).getReportRequestAccessedByContact());
                break;
            default:
                Assert.fail("Unknown user type provided: \"" + userType + "\" when verifying database");
        }
    }
    
}
