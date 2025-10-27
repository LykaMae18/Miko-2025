package com.indracompany.sysad.api;

import com.indracompany.acrsal.model.sales.ReportedSalesHist;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface AuditTrailService extends Serializable {
  Object getAuditTrailList(Map<String, Object> paramMap);
  
  List<ReportedSalesHist> getAuditTrailSalesReport(Map<String, Object> paramMap);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\sysad\api\AuditTrailService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */