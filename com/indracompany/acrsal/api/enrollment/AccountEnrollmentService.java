package com.indracompany.acrsal.api.enrollment;

import com.indracompany.acrsal.exception.DuplicateAccountEnrollmentException;
import com.indracompany.acrsal.exception.DuplicateUsernameException;
import com.indracompany.acrsal.exception.ITSValidationException;
import com.indracompany.acrsal.exception.NoMINException;
import com.indracompany.acrsal.forms.LineOfBusinessForm;
import com.indracompany.acrsal.models.AuthorizedUser;
import com.indracompany.acrsal.models.Business;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface AccountEnrollmentService extends Serializable {
  List<Business> getAcctEnrollmentList(Map<String, Object> paramMap, String paramString);
  
  List<Business> getAcctEnrollmentListForTaskManager(Map<String, Object> paramMap, String paramString);
  
  Business getAuthorizedUserByTransactionNo(String paramString);
  
  Business getBusinessInMain(AuthorizedUser paramAuthorizedUser, String paramString);
  
  boolean isUserExists(String paramString1, String paramString2) throws DuplicateUsernameException;
  
  boolean isBusinessBranchCodeValid(String paramString1, String paramString2) throws ITSValidationException;
  
  boolean isTinBrancCodeBusinessTypeValid(String paramString1, String paramString2, String paramString3) throws ITSValidationException;
  
  String addAccountEnrollmentToTemp(Map<String, Object> paramMap);
  
  void modifyUserContactDetails(Map<String, Object> paramMap);
  
  void modifyDeactivateActivate(Map<String, Object> paramMap);
  
  Business getAcctEnrollmentByTransactionNo(String paramString1, String paramString2);
  
  Business getAcctDetailsByUserNameAndLoginType(Map<String, Object> paramMap);
  
  Business getITSBusinessInfo(Map<String, Object> paramMap);
  
  boolean isBusinessAddBranchCodeExist(String paramString1, String paramString2, Map<String, Object> paramMap) throws DuplicateAccountEnrollmentException;
  
  Business getBusinessDetailBranch(Map<String, Object> paramMap);
  
  void approveAccount(Map<String, Object> paramMap);
  
  String getBusinessDetailsOfTINAndBranch(String paramString1, String paramString2);
  
  boolean isBusinessExistingInMain(String paramString1, String paramString2);
  
  String getRDOCodeByTINAndBranch(String paramString1, String paramString2);
  
  Business getAcctDetailsByUserLoginTypeAndTransNum(Map<String, Object> paramMap);
  
  List<Map<String, Object>> getUserEnrollmentHistory(String paramString);
  
  Business getBusinessDetailsOfTINBranchAccredCd(String paramString1, String paramString2, String paramString3);
  
  Business getBusinessDetailsByBusTIN(Map<String, Object> paramMap);
  
  boolean isBusinessNameValidFromITS(Map<String, Object> paramMap);
  
  List<LineOfBusinessForm> getLOBList();
  
  String getLOBByMITandCode(Map<Object, String> paramMap);
  
  int getMachinePermitCount(Map<Object, String> paramMap) throws NoMINException;
  
  void activateDeactivateUser(Map<String, Object> paramMap);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\enrollment\AccountEnrollmentService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */