package com.indracompany.acrsal.api.enrollment;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface TaskManagerService extends Serializable {
  List<Map<String, Object>> getStatusCountFromBusiness(String paramString);
  
  List<Map<String, Object>> getAccrediationStatusCount(Map<String, Object> paramMap);
  
  List<Map<String, Object>> getEnrollmentStatusCount(Map<String, Object> paramMap);
  
  List<Map<String, Object>> getRegistrationStatusCount(Map<String, Object> paramMap);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\enrollment\TaskManagerService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */