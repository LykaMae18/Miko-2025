package com.indracompany.acrsal.api.registration;

import com.indracompany.acrsal.models.AuditTrail;
import com.indracompany.acrsal.models.ParameterizedObject;
import com.indracompany.acrsal.models.reports.PermitToUse;
import com.indracompany.core.reporting.model.ReportContainer;
import java.io.Serializable;

public interface PermitService extends Serializable {
  PermitToUse getPermitToUseByPermitCode(String paramString1, String paramString2, String paramString3);
  
  ReportContainer printPermitToUse(String paramString1, PermitToUse paramPermitToUse, String paramString2, String paramString3, AuditTrail paramAuditTrail);
  
  ReportContainer printPermitToUseOfGlobal(String paramString1, PermitToUse paramPermitToUse, String paramString2, String paramString3, AuditTrail paramAuditTrail, String paramString4);
  
  String generateHashCode(String paramString);
  
  PermitToUse getPermitToUseDetailsByPermitCodeAndMIN(String paramString1, String paramString2);
  
  ParameterizedObject getStatusOfGlobalTerminal(String paramString);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\registration\PermitService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */