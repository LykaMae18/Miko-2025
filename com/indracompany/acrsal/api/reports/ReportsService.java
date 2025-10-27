package com.indracompany.acrsal.api.reports;

import com.indracompany.acrsal.forms.ReportCalendar;
import com.indracompany.acrsal.models.AuthorizedUser;
import com.indracompany.acrsal.models.ParameterizedObject;
import com.indracompany.core.reporting.model.ReportContainer;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ReportsService extends Serializable {
  List<ParameterizedObject> getFilteredSalesReportListByAuthorizedUser(AuthorizedUser paramAuthorizedUser);
  
  List<ParameterizedObject> getBIRRDOAccessByUserNameAndLoginType(Map<String, Object> paramMap);
  
  List<ParameterizedObject> getBIRRegionAccessByUserNameAndLoginType(Map<String, Object> paramMap);
  
  ReportContainer generateAmendedSalesReport(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  ReportContainer generateLateMonthlySalesReport(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  ReportContainer generateNoMonthlySalesReport(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  ReportContainer generateZeroMonthlySalesReport(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  ReportContainer generateMonthlySalesReport(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  int checkSalesRepsCount(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  int checkZeroSalesRepsCount(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  int getMonthlySalesCount(Map<String, Object> paramMap);
  
  int checkMachinesNoSalesCount(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  int getAmendedSalesListCount(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  int getLateSalesListCount(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  int getSalesLedgerSummaryCount(Map<String, Object> paramMap);
  
  ReportContainer generateSalesLedgerReport(Map<String, Object> paramMap, List<ReportCalendar> paramList);
  
  List<String> getComplianceReportPerTaxPayer(Map<String, Object> paramMap);
  
  List<String> getComplianceReportPerRDO(Map<String, Object> paramMap);
  
  List<String> getMatching(Map<String, Object> paramMap);
  
  ReportContainer generateComplianceReportPerTaxPayer(Map<String, Object> paramMap);
  
  ReportContainer generateComplianceReportPerRDO(Map<String, Object> paramMap);
  
  ReportContainer generateMatching(Map<String, Object> paramMap);
  
  ReportContainer generateAccreditedSoftwarePerSupplierReport(Map<String, Object> paramMap);
  
  ReportContainer generateListOfMachineWithFinalPermit(Map<String, Object> paramMap);
  
  ReportContainer generateListOfMachinesWithProvisionalPermit(Map<String, Object> paramMap);
  
  ReportContainer generateMachineRegistrationPerRDOGenerator(Map<String, Object> paramMap);
  
  ReportContainer generateListOfRegisteredSpecialPurposeMachines(Map<String, Object> paramMap);
  
  ReportContainer generateListOfRegisteredMachinesConvertedToFinalPermit(Map<String, Object> paramMap);
  
  ReportContainer generateListOfMachinesWithCancelledMinReport(Map<String, Object> paramMap);
  
  ReportContainer generateUserAccessReport(Map<String, Object> paramMap);
  
  int checkBusinessBranchFromITS(Map<String, Object> paramMap);
  
  List<ParameterizedObject> getReportListByProfile(Map<String, Object> paramMap);
  
  int getAllMachineRegistrationCount(Map<String, Object> paramMap);
  
  int getAccreditedSoftwarePerSupplierCount(Map<String, Object> paramMap);
  
  int getMachineWithFinalPermitCount(Map<String, Object> paramMap);
  
  int getMachinesWithProvisionalPermitCount(Map<String, Object> paramMap);
  
  int getSPMPerRDOCount(Map<String, Object> paramMap);
  
  int getMachinesWithCancelledMINCount(Map<String, Object> paramMap);
  
  int getMachinesConvertedToFinalPermitCount(Map<String, Object> paramMap);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\reports\ReportsService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */