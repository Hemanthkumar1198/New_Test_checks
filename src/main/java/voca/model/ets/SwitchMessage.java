package voca.model.ets;

import com.vocalink.bacs.test.exception.TestRunningException;
import static com.vocalink.bacs.model.ets.MessageType .*;

public enum SwitchMessage {
   
ACCEPT_ACCOUNT_SWITCH_T251(MSG02，  "Accept Switch T251" : “MSG02_Accept_T251"), 
REQUEST_PAYNENT(MSG08,  "Request Payment',  : "MSG08_Request_Payment"),， 
TERMINATE_ SWITCH(AS610 "Terminate Switch",  “MSG10_Terminate_Switch”), 
PAYMENT_RESPONSE(MS609 "Response Payment", : "MSG09_Payment_Response")，,
FULL_ACCOUNT_SWITCH(MS601，  ; IFull Account Switch",  "MSG01_Initiate_Request"), 
FULL_ACCOUNT_SWITCH_T145(;"Full Account Switch T145",   “MSG01_Initiate_Request_T145"), 
FULL_ACCOUNT_SWITCHL_T146(MSG01，  "Full Account Switch T146",filenames "MSG01_Initiate_Request_T146"),
FULL_ACCOUNT_SWITCH_T112(MSG01, ,messagelNames "Full Account Switch T112",   “MSG01_Initiate_Request_ T112"), 
REQUEST_REDIRECTIOML_SMITCH(MSG04,"Redirection', : "MSG04_Request_Redirection"), 
VALID_NO_PAY_EXCEPTION(NSG09，  "No Pay Response', "MSG09_Valid_No_Pay_Response')，
PARTIAL_ACCOUNT_SWITCH(MSGO1,   "Partial Account Switch",  : "MSG01_Initiate_Partial"),
BALANCE_ TRANSFER_REQUEST(MSG05, "Balance Transfer",  "MSG05_Reguest_Balance_Transfer")，
NOTIFY_SWITCH_COMPLETE(MSGO7, Notify Complete Switchr,  "MsGo7_Notify_Switch_Complete), 
ACCEPT_PARTIAL_ACCOUNT_SWITCH(MSGB2,  "Accept Partial Account Switch",   "MSG02.Partial_Accept")， 
CANCEL_PAYMENT_PARTIAL_SWITCH(MSG02,   "Cancel Payment Partial"，filename "MISG03_Cancel_Pyat_PartiaL_Smuitch"), // probably incorrected should be msg 3
BALANCE_TRANSFER_ACKNOWLEDGE_REQUEST(MSGO6，   "Balance Transfer Acknowledge", "MSG06_Balance_Transfer_Acknowledge");

private MessageType type; 
private Stping messagelane; 
private String temnlateFilename;

Switchlessage(MesageType type, String messagellame, String filename) {
this.type = type;
this,messageName s messageName;
this.templateFilenane s filename;
}

public static SwitchMessage getTemplateFop(Stning MeseageType){
for (SwitchMessage template i SwItchMessage,vaLNe6()){
if (template, messageName, equals(messageType)){
return template;
}
}
throm newr TestRunningException("Template for message type not found");
}


public String getTemnlateFilename(){
 return temnlateFilename;
}

public String getFilenameWithExtension(){
return templateFilename + ".xml";
}

public MessageType getMessageType() {
 returmn type;
}