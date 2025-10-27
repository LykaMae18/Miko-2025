package com.indracompany.acrsal.api.salesreport;

import com.indracompany.acrsal.exception.NoRecordFoundException;
import com.indracompany.acrsal.model.registration.Machine;
import com.indracompany.acrsal.models.AuthorizedUser;
import com.indracompany.acrsal.models.Branch;
import com.indracompany.acrsal.models.Business;
import com.indracompany.acrsal.models.ParameterizedObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SalesReportService extends Serializable {
  List<ParameterizedObject> generateStatus(Date paramDate, List<String> paramList, List<Date> paramList1);
  
  void addSalesReport(Map<String, Object> paramMap);
  
  List<Map<String, Object>> batchInsertOfSalesReport(List<Map<String, Object>> paramList);
  
  Machine getSalesReportBySRN(String paramString);
  
  List<Machine> getSalesReportList(Map<String, Object> paramMap) throws NoRecordFoundException;
  
  List<Business> getSalesReportListByCompany(Map<String, Object> paramMap) throws NoRecordFoundException;
  
  Business getBusinessDetails(String paramString1, String paramString2);
  
  List<String> getRDO();
  
  List<ParameterizedObject> getRDOWithDesc(Map<String, Object> paramMap);
  
  List<Branch> getBranchListOfSales(AuthorizedUser paramAuthorizedUser);
  
  List<Map<String, Object>> getMachineListByUser(String paramString1, String paramString2, String paramString3, String paramString4);
  
  Date getCancellationDate(String paramString);
  
  List<Map<String, Object>> getInitMachineListByUser(String paramString1, String paramString2, String paramString3, String paramString4);
  
  String uploadSalesReport(byte[] paramArrayOfbyte, String paramString1, String paramString2, String paramString3) throws FileNotFoundException, IOException;
  
  void processUploadFile();
  
  void processSMSFile();
  
  Date getEffectivityDateOfPermit(Map<String, Object> paramMap);
  
  void processEmailAttachment();
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\salesreport\SalesReportService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */