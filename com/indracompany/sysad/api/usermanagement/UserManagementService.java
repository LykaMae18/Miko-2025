package com.indracompany.sysad.api.usermanagement;

import com.indracompany.acrsal.model.sysad.BIRUser;
import com.indracompany.acrsal.model.sysad.UserAccess;
import com.indracompany.acrsal.models.AuditTrail;
import com.indracompany.acrsal.models.ParameterizedObject;
import com.indracompany.core.reporting.model.ReportContainer;
import com.indracompany.sysad.forms.ResetPasswordForm;
import com.indracompany.sysad.forms.UserListInquiryForm;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface UserManagementService extends Serializable {
  List<List<String>> getUserList();
  
  List<ParameterizedObject> getSystemReference(String paramString);
  
  List<ParameterizedObject> getOfficeList();
  
  List<ParameterizedObject> getDivisionList(String paramString);
  
  List<ParameterizedObject> getRoleList(String paramString);
  
  List<ParameterizedObject> getRoleList(String paramString1, String paramString2);
  
  List<UserListInquiryForm> getUserListForManagement(Map<String, Object> paramMap);
  
  List<UserAccess> getUserAccessList(Map<String, Object> paramMap, boolean paramBoolean);
  
  void updateAccess(Map<String, Object> paramMap);
  
  void clearModifiedAccess(String paramString);
  
  BIRUser getBIRUSerDetails(String paramString1, String paramString2);
  
  void modifyBIRUser(BIRUser paramBIRUser, AuditTrail paramAuditTrail);
  
  List<List<Object>> getUserAccessDetails(Map<String, Object> paramMap);
  
  ReportContainer exportBIRUserDetails(Map<String, Object> paramMap, List<BIRUser> paramList);
  
  ReportContainer passwordResetExport(Map<String, Object> paramMap, List<ResetPasswordForm> paramList);
  
  List<String[]> resetPasswordOfUser(Map<String, Object> paramMap, List<String[]> paramList);
  
  void insertBIRUserDetailsHist(Map<String, Object> paramMap);
  
  void insertIntoUserRoleHist(Map<String, Object> paramMap);
  
  String getRandomPassword();
  
  List<ParameterizedObject> getProfileList();
  
  void insertRecordUserAssignmentHist(Map<String, Object> paramMap);
  
  List<UserAccess> getUseSysUsersAssignments(Map<String, Object> paramMap);
  
  void modifyUserAssignment(Map<String, Object> paramMap);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\sysad\ap\\usermanagement\UserManagementService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */