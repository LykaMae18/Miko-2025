package com.indracompany.sysad.api;

import com.indracompany.acrsal.model.sysad.MenuFunction;
import com.indracompany.acrsal.models.ParameterizedObject;
import com.indracompany.sysad.forms.RDOAccessInquiryForm;
import com.indracompany.sysad.forms.RoleAccessInquiryForm;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface SysAdService extends Serializable {
  List<ParameterizedObject> getRoleList();
  
  List<ParameterizedObject> getOfficeListforRoleAccess();
  
  List<ParameterizedObject> getDivisionListforRoleAccess(String paramString);
  
  List<ParameterizedObject> getRoleListforRoleAccess(String paramString1, String paramString2);
  
  List<MenuFunction> getRoleMatrixListforRoleAccess(Map<String, Object> paramMap);
  
  List<MenuFunction> getMenuFunctionList(String paramString);
  
  void deleteRoleList(Map<String, Object> paramMap);
  
  void insertRoleAccess(Map<String, Object> paramMap);
  
  void deleteUserAssignments(Map<String, Object> paramMap);
  
  void insertUserAssignments(Map<String, Object> paramMap);
  
  int checkIfUserAssignmentExist(Map<String, Object> paramMap);
  
  List<ParameterizedObject> getUserLoginFromRole(String paramString);
  
  List<ParameterizedObject> getRdoList();
  
  List<ParameterizedObject> getCurrentRdoList(Map<String, Object> paramMap);
  
  void addRdoCodes(String paramString, Map<String, Object> paramMap);
  
  List<RoleAccessInquiryForm> getRoleAccessInquiryList(Map<String, Object> paramMap);
  
  List<ParameterizedObject> getReportTypes(String paramString);
  
  List<ParameterizedObject> getUserReportTypes(Map<String, Object> paramMap);
  
  void addReportsAccess(Map<String, Object> paramMap);
  
  List<RDOAccessInquiryForm> getRdoAccessesInquiry(Map<String, Object> paramMap);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\sysad\api\SysAdService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */