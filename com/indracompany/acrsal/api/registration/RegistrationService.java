package com.indracompany.acrsal.api.registration;

import com.indracompany.acrsal.exception.AccredittedSoftwareForProvRegistrationException;
import com.indracompany.acrsal.exception.ExistingRegistrationApplicationException;
import com.indracompany.acrsal.forms.ConversionInquiryResultForm;
import com.indracompany.acrsal.forms.MachineForm;
import com.indracompany.acrsal.forms.MachineUploadForm;
import com.indracompany.acrsal.forms.PermitHistoryForm;
import com.indracompany.acrsal.forms.PermitInquiryForm;
import com.indracompany.acrsal.forms.RegistrationPermitForm;
import com.indracompany.acrsal.forms.RegistrationViewForm;
import com.indracompany.acrsal.forms.UploadMachineValidationForm;
import com.indracompany.acrsal.model.registration.Machine;
import com.indracompany.acrsal.model.registration.MachineCRMDetails;
import com.indracompany.acrsal.model.registration.MachineOSMDetails;
import com.indracompany.acrsal.model.registration.MachinePOSDetails;
import com.indracompany.acrsal.model.registration.MachineResult;
import com.indracompany.acrsal.model.registration.MachineSPMDetails;
import com.indracompany.acrsal.models.AuditTrail;
import com.indracompany.acrsal.models.Business;
import com.indracompany.acrsal.models.ParameterizedObject;
import com.indracompany.core.reporting.model.ReportContainer;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface RegistrationService extends Serializable {
  Business getITSBusinessInfo(Map<String, Object> paramMap);
  
  int checkIfMachineExist(Map<String, Object> paramMap);
  
  int checkIfMachineExistsUpload(Map<String, Object> paramMap);
  
  MachineResult getMachine(Map<String, Object> paramMap);
  
  RegistrationViewForm getPermitDetailsByPermitCode(String paramString);
  
  List<MachineForm> getMachineListByPermitCode(String paramString);
  
  List<PermitHistoryForm> getPermitHistoryByPermitCode(String paramString);
  
  List<PermitInquiryForm> getRegistrationRecordList(Map<String, Object> paramMap);
  
  List<ConversionInquiryResultForm> getRegistrationRecordListForConversion(Map<String, Object> paramMap);
  
  String getRDOCodeByUserNameAndLoginType(Map<String, Object> paramMap);
  
  List<String> getMachineRelationShipByMIN(String paramString);
  
  int checkIfAccreditationNoExist(Map<String, Object> paramMap);
  
  String insertRegistrationCRMRecord(Map<String, Object> paramMap, String paramString);
  
  String insertRegistrationSPMRecord(Map<String, Object> paramMap, String paramString);
  
  String insertRegistrationOSMRecord(Map<String, Object> paramMap, String paramString);
  
  String insertRegistrationPOSRecord(Map<String, Object> paramMap, String paramString);
  
  void insertUserToBusinessRegistration(Map<String, Object> paramMap);
  
  List<Business> getBusinessByRDO(Map<String, Object> paramMap);
  
  ReportContainer exportCRMRegistration(Map<String, Object> paramMap, List<MachineCRMDetails> paramList);
  
  ReportContainer exportSPMRegistration(Map<String, Object> paramMap, List<MachineSPMDetails> paramList);
  
  ReportContainer exportOSMRegistration(Map<String, Object> paramMap, List<MachineOSMDetails> paramList);
  
  ReportContainer exportPOSStandAloneRegistration(Map<String, Object> paramMap, List<MachinePOSDetails> paramList);
  
  ReportContainer exportPOSConnectedRegistration(Map<String, Object> paramMap, List<MachinePOSDetails> paramList);
  
  List<List<String>> getAuthorizedUserOfBusiness(Map<String, Object> paramMap);
  
  boolean isUserAuthorizedToPreviewPermit(String paramString1, String paramString2, String paramString3, String paramString4);
  
  void updateUserStatusInRegistration(Map<String, Object> paramMap);
  
  int checkIfUserExist(Map<String, Object> paramMap);
  
  String checkIfAuthUserExist(Map<String, Object> paramMap);
  
  int checkIfRegistrationExist(Map<String, Object> paramMap);
  
  void updateRegistrationStatusPermit(Map<String, Object> paramMap);
  
  void updatePermitToCancel(Map<String, Object> paramMap);
  
  void updateProvToFinalRegistration(Map<String, Object> paramMap);
  
  ReportContainer printLetterOfCancellation(Map<String, Object> paramMap);
  
  RegistrationViewForm getPermitDetailsForCancellation(String paramString);
  
  String getReportRDOHeadName(String paramString1, String paramString2);
  
  void addMachineManagementOfDumbTerminal(Map<String, Object> paramMap);
  
  void removeOrReAddMachineManagementOfDumbTerminal(Map<String, Object> paramMap);
  
  List<PermitInquiryForm> getPermitsWithDumbTerminals(Map<String, Object> paramMap);
  
  List<Map<String, Object>> getRegistrationRecordListForCancellation(Map<String, Object> paramMap);
  
  List<ParameterizedObject> getBIRRDOAccessByUserNameAndLoginType(Map<String, Object> paramMap);
  
  void manageDumbTerminals(String paramString1, String paramString2, Machine paramMachine, List<Machine> paramList, AuditTrail paramAuditTrail);
  
  MachineForm getMachineDetailsByPermitCodeAndMIN(Map<String, Object> paramMap);
  
  void modifyMachineDetails(Map<String, Object> paramMap);
  
  int checkMachineDetails(Map<String, Object> paramMap);
  
  boolean SPMCheckIfValid(Map<String, Object> paramMap);
  
  boolean checkIfValidOSMProvisional(Map<String, Object> paramMap);
  
  boolean checkIfValidOSMFinal(Map<String, Object> paramMap);
  
  boolean checkIfAccreditatedBundled(Map<String, Object> paramMap);
  
  boolean checkBrandModelForSoftwareBundled(Map<String, Object> paramMap);
  
  boolean isPOSRegistrationValid(List<UploadMachineValidationForm> paramList, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, String[] paramArrayOfString) throws ExistingRegistrationApplicationException, AccredittedSoftwareForProvRegistrationException;
  
  int checkIfSerialExist(Map<String, Object> paramMap);
  
  MachineResult getMachineBySerial(Map<String, Object> paramMap);
  
  boolean CRMCheckIfValid(Map<String, Object> paramMap);
  
  String OSMCheckIfValid(Map<String, Object> paramMap);
  
  String getAccreditationDetailsExist(Map<String, Object> paramMap);
  
  Map<String, List<MachineUploadForm>> groupRegistrationByBusinessName(List<MachineUploadForm> paramList);
  
  Map<String, Object> insertUploadedRegistrationCRMRecord(Map<String, Object> paramMap, String paramString);
  
  Map<String, Object> insertUploadedRegistrationOSMRecord(Map<String, Object> paramMap, String paramString);
  
  Map<String, Object> insertUploadedRegistrationSPMRecord(Map<String, Object> paramMap, String paramString);
  
  Map<String, Object> insertUploadedRegistrationPOSRecord(Map<String, Object> paramMap, String paramString);
  
  List<UploadMachineValidationForm> getAllActiveRegisteredMachines();
  
  List<UploadMachineValidationForm> getAllActiveRegisteredMachinesLikeSerial(String paramString);
  
  ReportContainer exportUploadedCRMRegistration(Map<String, Object> paramMap, List<MachineUploadForm> paramList);
  
  ReportContainer exportUploadedOSMRegistration(Map<String, Object> paramMap, List<MachineUploadForm> paramList);
  
  ReportContainer exportUploadedSPMRegistration(Map<String, Object> paramMap, List<MachineUploadForm> paramList);
  
  ReportContainer exportUploadedPOSSARegistration(Map<String, Object> paramMap, List<MachineUploadForm> paramList);
  
  ReportContainer exportUploadedPOSWTRegistration(Map<String, Object> paramMap, List<MachineUploadForm> paramList);
  
  List<MachineForm> getMachineListByPermitCodeGlobal(String paramString);
  
  RegistrationPermitForm getPermitDetailsByTransNum(String paramString);
  
  RegistrationPermitForm getPermitDetailsByMIN(String paramString);
  
  MachineForm getMachineDetailsByMIN(String paramString);
  
  Business getBusinessDetailsFromITS(String paramString1, String paramString2);
  
  String getBusinessNameFromITS(String paramString1, String paramString2);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\registration\RegistrationService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */