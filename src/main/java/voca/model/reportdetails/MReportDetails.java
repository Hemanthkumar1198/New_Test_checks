package voca.model.reportdetails;

public class MReportDetails {

    private String reportRequestID;

    private Date processingDate;

    public String getReportRequestID() {
        return reportRequestID;
    }

    public void setProcessingDate(Date processingDate) {
        this.processingDate = processingDate;
    }

    public Date getProcessingDate() {
        return processingDate;
    }

    public void setReportRequestID(String reportRequestID) {
        this.reportRequestID = reportRequestID;
    }
