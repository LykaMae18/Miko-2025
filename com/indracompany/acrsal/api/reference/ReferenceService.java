package com.indracompany.acrsal.api.reference;

import com.indracompany.acrsal.models.ParameterizedObject;
import com.indracompany.sysad.forms.ListOfValuesDisplayForm;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ReferenceService extends Serializable {
  List<ParameterizedObject> getReferenceObjects(String paramString);
  
  List<ParameterizedObject> getStatusListForApproval(String paramString);
  
  List<ParameterizedObject> getListOfValues(Map<String, Object> paramMap);
  
  List<ListOfValuesDisplayForm> getReferenceForAddDisplay(String paramString);
  
  void insertReference(Map<String, Object> paramMap);
  
  void updateReference(Map<String, Object> paramMap);
  
  void deleteReference(String paramString);
  
  Map<String, Object> getLOV(Map<String, Object> paramMap);
  
  ParameterizedObject getReferenceObject(String paramString);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\reference\ReferenceService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */