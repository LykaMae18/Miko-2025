package com.indracompany.acrsal.api.accreditation;

import com.indracompany.acrsal.exception.DuplicateProductException;
import com.indracompany.acrsal.forms.UploadSoftwareValidationForm;
import com.indracompany.acrsal.model.accreditation.Accreditation;
import com.indracompany.acrsal.model.accreditation.Checklist;
import com.indracompany.acrsal.models.AuditTrail;
import com.indracompany.acrsal.models.Business;
import com.indracompany.acrsal.models.ParameterizedObject;
import com.indracompany.core.reporting.model.ReportContainer;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface AccreditationService extends Serializable {
  String insertAccreditation(Map<String, Object> paramMap);
  
  List<Map<String, Object>> getAccreditationList(Map<String, Object> paramMap);
  
  List<Map<String, Object>> getAccreditationListForRevocation(Map<String, Object> paramMap);
  
  String getRDOCodeByUserNameAndLoginType(Map<String, Object> paramMap);
  
  List<ParameterizedObject> getBIRRDOAccessByUserNameAndLoginType(Map<String, Object> paramMap);
  
  boolean checkIfAccreditationExist(Map<String, Object> paramMap) throws DuplicateProductException;
  
  Accreditation getAccreditationByAccredNo(String paramString);
  
  Accreditation getAccreditationByTransactionNo(String paramString);
  
  List<Map<String, Object>> getAccreditationHistory(String paramString);
  
  List<Checklist> getAccreditationCheckList(String paramString);
  
  ReportContainer exportAccreditationApplication(Map<String, Object> paramMap);
  
  List<ParameterizedObject> getAccreditationStatus();
  
  String updateAccreditation(Map<String, Object> paramMap);
  
  void updateApprovalStatus(Map<String, Object> paramMap);
  
  void saveAccreditationRequirements(Map<String, Object> paramMap);
  
  ReportContainer printDenialLetter(Map<String, Object> paramMap);
  
  void insertAccreditationReportCounter(String paramString1, String paramString2, String paramString3, AuditTrail paramAuditTrail);
  
  void updateAccreditationReportCopyCounter(String paramString1, String paramString2, String paramString3, AuditTrail paramAuditTrail);
  
  void updateAccreditationReportAsOrigCounter(String paramString1, String paramString2, String paramString3, AuditTrail paramAuditTrail);
  
  Integer getReportCounter(String paramString1, String paramString2);
  
  String getReportAccreditationHashCode(String paramString1, String paramString2);
  
  String generateHashCode(String paramString);
  
  String getReportRDOHeadName(String paramString);
  
  String getRRNumber(String paramString);
  
  ParameterizedObject getRRDescription(String paramString);
  
  Business getBusinessFromProduct(Map<String, Object> paramMap);
  
  void testDeleteInsertStatus(Map<String, Object> paramMap);
  
  void revokeAccreditation(Map<String, Object> paramMap);
  
  void processAccreditation(Map<String, Object> paramMap);
  
  ReportContainer exportLetterOfRevocation(Map<String, Object> paramMap);
  
  ReportContainer generateAccreditationCertificate(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5);
  
  List<Map<String, Object>> getAccredittedProductDetailsList(Map<String, Object> paramMap, String paramString1, String paramString2);
  
  String getProductAccreditationNo(Map<String, Object> paramMap);
  
  List<UploadSoftwareValidationForm> getAllAccreditedSoftware(String paramString);
  
  UploadSoftwareValidationForm getAllAccreditedDetailByAccreditationNo(String paramString);
  
  Integer isBusinessNameRegistered(Map<String, Object> paramMap);
  
  List<String> getAuthRepInfo(Business paramBusiness, String paramString, boolean paramBoolean);
  
  void updateBusinessReps(Map<String, Object> paramMap);
  
  List<Map<String, Object>> getApprovedAccreditationList();
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\accreditation\AccreditationService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */