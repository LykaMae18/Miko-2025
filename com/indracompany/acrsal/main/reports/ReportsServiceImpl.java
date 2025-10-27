/*      */ package com.indracompany.acrsal.main.reports;
/*      */ 
/*      */ import com.indracompany.acrsal.api.reports.ReportsService;
/*      */ import com.indracompany.acrsal.dao.business.BusinessDao;
/*      */ import com.indracompany.acrsal.dao.reports.ReportsDao;
/*      */ import com.indracompany.acrsal.forms.ReportCalendar;
/*      */ import com.indracompany.acrsal.models.AuthorizedUser;
/*      */ import com.indracompany.acrsal.models.ParameterizedObject;
/*      */ import com.indracompany.acrsal.models.reports.AccreditedMachinesPerSupplierPerRDO;
/*      */ import com.indracompany.acrsal.models.reports.MachineRegistrationPerRDO;
/*      */ import com.indracompany.acrsal.models.reports.MachineWithFinalPermit;
/*      */ import com.indracompany.acrsal.models.reports.MachinesWithCancelledMIN;
/*      */ import com.indracompany.acrsal.models.reports.MachinesWithProvisionalPermit;
/*      */ import com.indracompany.acrsal.models.reports.RegisteredMachinesConvertedToFinalPermit;
/*      */ import com.indracompany.acrsal.models.reports.RegisteredSpecialPurposeMachines;
/*      */ import com.indracompany.acrsal.models.reports.TaxpayerSalesLedgerDetailsPerMachine;
/*      */ import com.indracompany.acrsal.models.reports.TaxpayerSalesLedgerPerMachine;
/*      */ import com.indracompany.acrsal.models.reports.TaxpayerWithAmendedSalesReport;
/*      */ import com.indracompany.acrsal.models.reports.TaxpayerWithLateSalesReportSubmission;
/*      */ import com.indracompany.acrsal.models.reports.TaxpayerWithMonthlySalesReportSubmission;
/*      */ import com.indracompany.acrsal.models.reports.TaxpayerWithNoMonthlySalesReportSubmission;
/*      */ import com.indracompany.acrsal.models.reports.TaxpayerWithZeroMonthlySalesReport;
/*      */ import com.indracompany.acrsal.models.reports.UserAccessSearchReport;
/*      */ import com.indracompany.core.reporting.ReportGenerator;
/*      */ import com.indracompany.core.reporting.model.ReportContainer;
/*      */ import com.indracompany.core.reporting.model.ReportFormat;
/*      */ import com.indracompany.core.util.ZipUtils;
/*      */ import com.indracompany.core.util.model.ZipDetail;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.IOException;
/*      */ import java.text.ParseException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.zip.ZipException;
/*      */ import net.sf.jasperreports.engine.JRDataSource;
/*      */ import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
/*      */ import org.apache.commons.io.IOUtils;
/*      */ import org.apache.log4j.Logger;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class ReportsServiceImpl
/*      */   implements ReportsService
/*      */ {
/*      */   private static final long serialVersionUID = 1288081959426005730L;
/*   52 */   private final Logger log = Logger.getLogger(ReportsServiceImpl.class);
/*      */   
/*      */   private ReportsDao reportsDao;
/*      */   
/*      */   private BusinessDao businessDao;
/*      */   
/*      */   private ReportGenerator reportsAmendedSalesGenerator;
/*      */   
/*      */   private ReportGenerator reportsLateMonthlySalesGenerator;
/*      */   
/*      */   private ReportGenerator reportsNoMonthlySalesGenerator;
/*      */   
/*      */   private ReportGenerator reportsZeroMonthlySalesGenerator;
/*      */   
/*      */   private ReportGenerator reportsMonthlySalesGenerator;
/*      */   
/*      */   private ReportGenerator reportsSalesLedgerGenerator;
/*      */   private ReportGenerator reportsAccreditedSoftwarePerSupplierGenerator;
/*      */   private ReportGenerator reportsListOfMachineWithFinalPermit;
/*      */   private ReportGenerator reportsMachineWithProvisionalPermitGenerator;
/*      */   private ReportGenerator reportsMachineRegistrationPerRDOGenerator;
/*      */   private ReportGenerator reportsRegisteredSpecialPurposeMachinesGenerator;
/*      */   private ReportGenerator reportsRegisteredMachinesConvertedToFinalGenerator;
/*      */   private ReportGenerator reportsMachinesWithCancelledMINGenerator;
/*      */   private ReportGenerator reportsUserAccessGenerator;
/*      */   private String reportsAmendedSalesFileName;
/*      */   private String reportsLateMonthlySalesFileName;
/*      */   private String reportsNoMonthlySalesFileName;
/*      */   private String reportsZeroMonthlySalesFileName;
/*      */   private String reportsMonthlySalesFileName;
/*      */   private String reportsAccreditedSoftwarePerSupplierFileName;
/*      */   private String reportsRegisteredSpecialPurposeMachinesFileName;
/*      */   private String managementReportLocation;
/*      */   
/*      */   public List<ParameterizedObject> getFilteredSalesReportListByAuthorizedUser(AuthorizedUser authorizedUser) {
/*   87 */     return this.reportsDao.getFilteredSalesReportListByAuthorizedUser(authorizedUser);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<ParameterizedObject> getBIRRDOAccessByUserNameAndLoginType(Map<String, Object> param) {
/*   93 */     return this.businessDao.getBIRRDOAccessByUserNameAndLoginType(param);
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer generateAmendedSalesReport(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*   98 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  100 */     if (reportCalList.size() == 1) {
/*      */       
/*  102 */       paramMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(0)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(0)).getYearReport());
/*  103 */       paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getMonthReport()));
/*  104 */       paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getYearReport()));
/*  105 */       List<TaxpayerWithAmendedSalesReport> reportList = new ArrayList<TaxpayerWithAmendedSalesReport>();
/*  106 */       reportList = this.reportsDao.getAmendedSalesList(paramMap);
/*      */       
/*  108 */       return this.reportsAmendedSalesGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  113 */     List<ZipDetail> zipList = new ArrayList<ZipDetail>();
/*      */     
/*  115 */     for (int i = 0; i < reportCalList.size(); i++) {
/*      */       
/*  117 */       Map<String, Object> reportMap = new HashMap<String, Object>();
/*  118 */       reportMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getMonthReport()));
/*  119 */       reportMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getYearReport()));
/*  120 */       reportMap.put("REPORT_TYPE", paramMap.get("REPORT_TYPE"));
/*  121 */       reportMap.put("USER_NAME", paramMap.get("USER_NAME"));
/*  122 */       reportMap.put("RDO_DESC", paramMap.get("RDO_DESC"));
/*  123 */       reportMap.put("RDO_CODE", paramMap.get("RDO_CODE"));
/*  124 */       reportMap.put("BUSINESS_TIN", paramMap.get("BUSINESS_TIN"));
/*  125 */       reportMap.put("BRANCH_CODE", paramMap.get("BRANCH_CODE"));
/*  126 */       reportMap.put("TIME_STAMP", paramMap.get("TIME_STAMP"));
/*  127 */       List<TaxpayerWithAmendedSalesReport> reportList = new ArrayList<TaxpayerWithAmendedSalesReport>();
/*      */       
/*  129 */       reportList = this.reportsDao.getAmendedSalesList(reportMap);
/*      */       
/*  131 */       if (reportList.size() > 0) {
/*      */         
/*  133 */         reportMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(i)).getYearReport());
/*  134 */         ReportContainer container = this.reportsAmendedSalesGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*  139 */         ZipDetail zipDetail = new ZipDetail(container.getReport(), container.getReportFilename() + ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "-" + ((ReportCalendar)reportCalList.get(i)).getYearReport(), container.getReportFormat().toString());
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*  145 */         zipList.add(zipDetail);
/*      */       } 
/*      */     } 
/*      */     
/*  149 */     ReportContainer zipContainer = null;
/*      */ 
/*      */     
/*      */     try {
/*  153 */       zipContainer = new ReportContainer(ZipUtils.compressMultipleFiles(zipList), this.reportsAmendedSalesFileName, ReportFormat.ZIP);
/*      */     
/*      */     }
/*  156 */     catch (ZipException e) {
/*      */       
/*  158 */       this.log.info(e);
/*      */     } 
/*      */     
/*  161 */     return zipContainer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void setReportsListOfMachineWithFinalPermit(ReportGenerator reportsListOfMachineWithFinalPermit) {
/*  167 */     this.reportsListOfMachineWithFinalPermit = reportsListOfMachineWithFinalPermit;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer generateLateMonthlySalesReport(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  172 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  174 */     if (reportCalList.size() == 1) {
/*      */       
/*  176 */       paramMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(0)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(0)).getYearReport());
/*  177 */       paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getMonthReport()));
/*  178 */       paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getYearReport()));
/*  179 */       List<TaxpayerWithLateSalesReportSubmission> reportList = new ArrayList<TaxpayerWithLateSalesReportSubmission>();
/*      */ 
/*      */       
/*  182 */       reportList = this.reportsDao.getLateSalesList(paramMap);
/*      */       
/*  184 */       return this.reportsLateMonthlySalesGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  189 */     List<ZipDetail> zipList = new ArrayList<ZipDetail>();
/*      */     
/*  191 */     for (int i = 0; i < reportCalList.size(); i++) {
/*      */ 
/*      */       
/*  194 */       Map<String, Object> reportMap = new HashMap<String, Object>();
/*  195 */       reportMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getMonthReport()));
/*      */       
/*  197 */       reportMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getYearReport()));
/*      */ 
/*      */       
/*  200 */       reportMap.put("REPORT_TYPE", paramMap.get("REPORT_TYPE"));
/*  201 */       reportMap.put("USER_NAME", paramMap.get("USER_NAME"));
/*  202 */       reportMap.put("RDO_DESC", paramMap.get("RDO_DESC"));
/*  203 */       reportMap.put("RDO_CODE", paramMap.get("RDO_CODE"));
/*  204 */       reportMap.put("BUSINESS_TIN", paramMap.get("BUSINESS_TIN"));
/*  205 */       reportMap.put("BRANCH_CODE", paramMap.get("BRANCH_CODE"));
/*  206 */       reportMap.put("TIME_STAMP", paramMap.get("TIME_STAMP"));
/*      */       
/*  208 */       List<TaxpayerWithLateSalesReportSubmission> reportList = new ArrayList<TaxpayerWithLateSalesReportSubmission>();
/*  209 */       reportList = this.reportsDao.getLateSalesList(reportMap);
/*      */       
/*  211 */       if (reportList.size() > 0) {
/*      */         
/*  213 */         reportMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(i)).getYearReport());
/*  214 */         ReportContainer container = this.reportsLateMonthlySalesGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */ 
/*      */         
/*  217 */         ZipDetail zipDetail = new ZipDetail(container.getReport(), container.getReportFilename() + ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "-" + ((ReportCalendar)reportCalList.get(i)).getYearReport(), container.getReportFormat().toString());
/*      */ 
/*      */ 
/*      */         
/*  221 */         zipList.add(zipDetail);
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  226 */     ReportContainer zipContainer = null;
/*      */ 
/*      */     
/*      */     try {
/*  230 */       zipContainer = new ReportContainer(ZipUtils.compressMultipleFiles(zipList), this.reportsLateMonthlySalesFileName, ReportFormat.ZIP);
/*      */     
/*      */     }
/*  233 */     catch (ZipException e) {
/*      */       
/*  235 */       this.log.info(e);
/*      */     } 
/*      */     
/*  238 */     return zipContainer;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateMonthlySalesReport(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  245 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  247 */     if (reportCalList.size() == 1) {
/*      */       
/*  249 */       paramMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(0)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(0)).getYearReport());
/*  250 */       paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getMonthReport()));
/*  251 */       paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getYearReport()));
/*  252 */       List<TaxpayerWithMonthlySalesReportSubmission> reportList = new ArrayList<TaxpayerWithMonthlySalesReportSubmission>();
/*      */       
/*  254 */       reportList = this.reportsDao.getMonthlySalesList(paramMap);
/*      */       
/*  256 */       return this.reportsMonthlySalesGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  261 */     List<ZipDetail> zipList = new ArrayList<ZipDetail>();
/*      */     
/*  263 */     for (int i = 0; i < reportCalList.size(); i++) {
/*      */ 
/*      */       
/*  266 */       Map<String, Object> reportMap = new HashMap<String, Object>();
/*  267 */       reportMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getMonthReport()));
/*      */       
/*  269 */       reportMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getYearReport()));
/*      */ 
/*      */       
/*  272 */       reportMap.put("REPORT_TYPE", paramMap.get("REPORT_TYPE"));
/*  273 */       reportMap.put("USER_NAME", paramMap.get("USER_NAME"));
/*  274 */       reportMap.put("RDO_DESC", paramMap.get("RDO_DESC"));
/*  275 */       reportMap.put("RDO_CODE", paramMap.get("RDO_CODE"));
/*  276 */       reportMap.put("BUSINESS_TIN", paramMap.get("BUSINESS_TIN"));
/*  277 */       reportMap.put("BRANCH_CODE", paramMap.get("BRANCH_CODE"));
/*  278 */       reportMap.put("TIME_STAMP", paramMap.get("TIME_STAMP"));
/*      */       
/*  280 */       List<TaxpayerWithMonthlySalesReportSubmission> reportList = new ArrayList<TaxpayerWithMonthlySalesReportSubmission>();
/*  281 */       reportList = this.reportsDao.getMonthlySalesList(reportMap);
/*      */       
/*  283 */       if (null != reportList && reportList.size() > 0) {
/*      */         
/*  285 */         reportMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(i)).getYearReport());
/*  286 */         ReportContainer container = this.reportsMonthlySalesGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */ 
/*      */         
/*  289 */         ZipDetail zipDetail = new ZipDetail(container.getReport(), container.getReportFilename() + ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "-" + ((ReportCalendar)reportCalList.get(i)).getYearReport(), container.getReportFormat().toString());
/*      */ 
/*      */ 
/*      */         
/*  293 */         zipList.add(zipDetail);
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  298 */     ReportContainer zipContainer = null;
/*      */ 
/*      */     
/*      */     try {
/*  302 */       zipContainer = new ReportContainer(ZipUtils.compressMultipleFiles(zipList), this.reportsMonthlySalesFileName, ReportFormat.ZIP);
/*      */     
/*      */     }
/*  305 */     catch (ZipException e) {
/*      */       
/*  307 */       this.log.info(e);
/*      */     } 
/*      */     
/*  310 */     return zipContainer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateNoMonthlySalesReport(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  316 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  318 */     if (reportCalList.size() == 1) {
/*      */       
/*  320 */       paramMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(0)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(0)).getYearReport());
/*  321 */       paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getMonthReport()));
/*  322 */       paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getYearReport()));
/*      */       
/*  324 */       List<TaxpayerWithNoMonthlySalesReportSubmission> reportList = new ArrayList<TaxpayerWithNoMonthlySalesReportSubmission>();
/*      */       
/*  326 */       reportList = this.reportsDao.getNoMonthlySalesList(paramMap);
/*      */       
/*  328 */       return this.reportsNoMonthlySalesGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  333 */     List<ZipDetail> zipList = new ArrayList<ZipDetail>();
/*      */     
/*  335 */     for (int i = 0; i < reportCalList.size(); i++) {
/*      */       
/*  337 */       Map<String, Object> reportMap = new HashMap<String, Object>();
/*  338 */       reportMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getMonthReport()));
/*      */       
/*  340 */       reportMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getYearReport()));
/*      */ 
/*      */       
/*  343 */       reportMap.put("REPORT_TYPE", paramMap.get("REPORT_TYPE"));
/*  344 */       reportMap.put("USER_NAME", paramMap.get("USER_NAME"));
/*  345 */       reportMap.put("RDO_DESC", paramMap.get("RDO_DESC"));
/*  346 */       reportMap.put("RDO_CODE", paramMap.get("RDO_CODE"));
/*  347 */       reportMap.put("BUSINESS_TIN", paramMap.get("BUSINESS_TIN"));
/*  348 */       reportMap.put("BRANCH_CODE", paramMap.get("BRANCH_CODE"));
/*  349 */       reportMap.put("TIME_STAMP", paramMap.get("TIME_STAMP"));
/*      */       
/*  351 */       List<TaxpayerWithNoMonthlySalesReportSubmission> reportList = new ArrayList<TaxpayerWithNoMonthlySalesReportSubmission>();
/*  352 */       reportList = this.reportsDao.getNoMonthlySalesList(reportMap);
/*      */       
/*  354 */       if (reportList.size() > 0) {
/*      */         
/*  356 */         reportMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(i)).getYearReport());
/*      */         
/*  358 */         ReportContainer container = this.reportsNoMonthlySalesGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */ 
/*      */         
/*  361 */         ZipDetail zipDetail = new ZipDetail(container.getReport(), container.getReportFilename() + ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "-" + ((ReportCalendar)reportCalList.get(i)).getYearReport(), container.getReportFormat().toString());
/*      */ 
/*      */ 
/*      */         
/*  365 */         zipList.add(zipDetail);
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  371 */     ReportContainer zipContainer = null;
/*      */ 
/*      */     
/*      */     try {
/*  375 */       zipContainer = new ReportContainer(ZipUtils.compressMultipleFiles(zipList), this.reportsNoMonthlySalesFileName, ReportFormat.ZIP);
/*      */     
/*      */     }
/*  378 */     catch (ZipException e) {
/*      */       
/*  380 */       this.log.info(e);
/*      */     } 
/*      */     
/*  383 */     return zipContainer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateZeroMonthlySalesReport(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  389 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  391 */     if (reportCalList.size() == 1) {
/*      */       
/*  393 */       paramMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(0)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(0)).getYearReport());
/*  394 */       paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getMonthReport()));
/*  395 */       paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getYearReport()));
/*  396 */       List<TaxpayerWithZeroMonthlySalesReport> reportList = new ArrayList<TaxpayerWithZeroMonthlySalesReport>();
/*      */       
/*  398 */       reportList = this.reportsDao.getZeroMonthlySalesList(paramMap);
/*      */       
/*  400 */       return this.reportsZeroMonthlySalesGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  405 */     List<ZipDetail> zipList = new ArrayList<ZipDetail>();
/*      */     
/*  407 */     for (int i = 0; i < reportCalList.size(); i++) {
/*      */       
/*  409 */       Map<String, Object> reportMap = new HashMap<String, Object>();
/*  410 */       reportMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getMonthReport()));
/*      */       
/*  412 */       reportMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getYearReport()));
/*      */ 
/*      */       
/*  415 */       reportMap.put("REPORT_TYPE", paramMap.get("REPORT_TYPE"));
/*  416 */       reportMap.put("USER_NAME", paramMap.get("USER_NAME"));
/*  417 */       reportMap.put("RDO_DESC", paramMap.get("RDO_DESC"));
/*  418 */       reportMap.put("RDO_CODE", paramMap.get("RDO_CODE"));
/*  419 */       reportMap.put("BUSINESS_TIN", paramMap.get("BUSINESS_TIN"));
/*  420 */       reportMap.put("BRANCH_CODE", paramMap.get("BRANCH_CODE"));
/*  421 */       reportMap.put("TIME_STAMP", paramMap.get("TIME_STAMP"));
/*      */       
/*  423 */       List<TaxpayerWithZeroMonthlySalesReport> reportList = new ArrayList<TaxpayerWithZeroMonthlySalesReport>();
/*  424 */       reportList = this.reportsDao.getZeroMonthlySalesList(reportMap);
/*      */       
/*  426 */       if (reportList.size() > 0) {
/*      */         
/*  428 */         reportMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(i)).getYearReport());
/*  429 */         ReportContainer container = this.reportsZeroMonthlySalesGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */ 
/*      */         
/*  432 */         ZipDetail zipDetail = new ZipDetail(container.getReport(), container.getReportFilename() + ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "-" + ((ReportCalendar)reportCalList.get(i)).getYearReport(), container.getReportFormat().toString());
/*      */ 
/*      */ 
/*      */         
/*  436 */         zipList.add(zipDetail);
/*      */       } 
/*      */     } 
/*      */     
/*  440 */     ReportContainer zipContainer = null;
/*      */ 
/*      */     
/*      */     try {
/*  444 */       zipContainer = new ReportContainer(ZipUtils.compressMultipleFiles(zipList), this.reportsZeroMonthlySalesFileName, ReportFormat.ZIP);
/*      */     
/*      */     }
/*  447 */     catch (ZipException e) {
/*      */       
/*  449 */       this.log.info(e);
/*      */     } 
/*      */     
/*  452 */     return zipContainer;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private ReportFormat getReportFormat(String format) {
/*  459 */     if (format.equals("PDF"))
/*      */     {
/*  461 */       return ReportFormat.PDF;
/*      */     }
/*  463 */     if (format.equals("CSV"))
/*      */     {
/*  465 */       return ReportFormat.CSV;
/*      */     }
/*  467 */     if (format.equals("XLS"))
/*      */     {
/*  469 */       return ReportFormat.XLS;
/*      */     }
/*      */ 
/*      */     
/*  473 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int checkSalesRepsCount(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  491 */     return this.reportsDao.checkSalesRepsCount(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public int checkZeroSalesRepsCount(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  496 */     return this.reportsDao.checkZeroSalesRepsCount(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getAmendedSalesListCount(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  502 */     int count = 0;
/*      */     
/*  504 */     for (int i = 0; i < reportCalList.size(); i++) {
/*      */       
/*  506 */       paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getMonthReport()));
/*      */       
/*  508 */       paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getYearReport()));
/*      */ 
/*      */       
/*  511 */       count += this.reportsDao.getAmendedSalesListCount(paramMap);
/*      */     } 
/*      */     
/*  514 */     return count;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getLateSalesListCount(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  520 */     int count = 0;
/*      */     
/*  522 */     for (int i = 0; i < reportCalList.size(); i++) {
/*      */       
/*  524 */       paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getMonthReport()));
/*      */       
/*  526 */       paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getYearReport()));
/*      */ 
/*      */       
/*  529 */       count += this.reportsDao.getLateSalesListCount(paramMap);
/*      */     } 
/*      */     
/*  532 */     return count;
/*      */   }
/*      */ 
/*      */   
/*      */   public int checkMachinesNoSalesCount(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  537 */     int returnValue = 0;
/*  538 */     paramMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(0)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(0)).getYearReport());
/*  539 */     paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getMonthReport()));
/*  540 */     paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(0)).getYearReport()));
/*  541 */     if (reportCalList.size() == 1) {
/*      */ 
/*      */       
/*  544 */       returnValue = this.reportsDao.checkMachinesNoSalesCount(paramMap);
/*      */     
/*      */     }
/*      */     else {
/*      */       
/*  549 */       for (int i = 0; i < reportCalList.size(); i++) {
/*      */         
/*  551 */         paramMap.remove("SALES_DATE");
/*  552 */         paramMap.remove("REPORT_MONTH");
/*  553 */         paramMap.remove("REPORT_YEAR");
/*      */         
/*  555 */         paramMap.put("SALES_DATE", ((ReportCalendar)reportCalList.get(i)).getMonthReport() + "/" + ((ReportCalendar)reportCalList.get(i)).getYearReport());
/*  556 */         paramMap.put("REPORT_MONTH", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getMonthReport()));
/*  557 */         paramMap.put("REPORT_YEAR", Integer.valueOf(((ReportCalendar)reportCalList.get(i)).getYearReport()));
/*      */         
/*  559 */         returnValue += this.reportsDao.checkMachinesNoSalesCount(paramMap);
/*      */       } 
/*      */     } 
/*      */     
/*  563 */     return returnValue;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsDao(ReportsDao reportsDao) {
/*  568 */     this.reportsDao = reportsDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setBusinessDao(BusinessDao businessDao) {
/*  573 */     this.businessDao = businessDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsAmendedSalesGenerator(ReportGenerator reportsAmendedSalesGenerator) {
/*  578 */     this.reportsAmendedSalesGenerator = reportsAmendedSalesGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsLateMonthlySalesGenerator(ReportGenerator reportsLateMonthlySalesGenerator) {
/*  583 */     this.reportsLateMonthlySalesGenerator = reportsLateMonthlySalesGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsNoMonthlySalesGenerator(ReportGenerator reportsNoMonthlySalesGenerator) {
/*  588 */     this.reportsNoMonthlySalesGenerator = reportsNoMonthlySalesGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsZeroMonthlySalesGenerator(ReportGenerator reportsZeroMonthlySalesGenerator) {
/*  593 */     this.reportsZeroMonthlySalesGenerator = reportsZeroMonthlySalesGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsMonthlySalesGenerator(ReportGenerator reportsMonthlySalesGenerator) {
/*  598 */     this.reportsMonthlySalesGenerator = reportsMonthlySalesGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsAmendedSalesFileName(String reportsAmendedSalesFileName) {
/*  603 */     this.reportsAmendedSalesFileName = reportsAmendedSalesFileName;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsLateMonthlySalesFileName(String reportsLateMonthlySalesFileName) {
/*  608 */     this.reportsLateMonthlySalesFileName = reportsLateMonthlySalesFileName;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsNoMonthlySalesFileName(String reportsNoMonthlySalesFileName) {
/*  613 */     this.reportsNoMonthlySalesFileName = reportsNoMonthlySalesFileName;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsZeroMonthlySalesFileName(String reportsZeroMonthlySalesFileName) {
/*  618 */     this.reportsZeroMonthlySalesFileName = reportsZeroMonthlySalesFileName;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsMonthlySalesFileName(String reportsMonthlySalesFileName) {
/*  623 */     this.reportsMonthlySalesFileName = reportsMonthlySalesFileName;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateSalesLedgerReport(Map<String, Object> paramMap, List<ReportCalendar> reportCalList) {
/*  629 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*  630 */     List<TaxpayerSalesLedgerPerMachine> dummyReportList = new ArrayList<TaxpayerSalesLedgerPerMachine>();
/*  631 */     List<TaxpayerSalesLedgerPerMachine> reportList = new ArrayList<TaxpayerSalesLedgerPerMachine>();
/*  632 */     dummyReportList = this.reportsDao.getSalesLedgerMachineList(paramMap);
/*  633 */     for (TaxpayerSalesLedgerPerMachine dummyReportItem : dummyReportList) {
/*      */       
/*  635 */       boolean recordFound = false;
/*      */       
/*  637 */       for (TaxpayerSalesLedgerPerMachine reportItem : reportList) {
/*      */         
/*  639 */         if (reportItem.getMin().equalsIgnoreCase(dummyReportItem.getMin())) {
/*      */           
/*  641 */           recordFound = true;
/*      */           break;
/*      */         } 
/*      */       } 
/*  645 */       if (!recordFound)
/*      */       {
/*  647 */         reportList.add(dummyReportItem);
/*      */       }
/*      */     } 
/*      */ 
/*      */     
/*  652 */     paramMap.put("TAXPAYER_NAME", ((TaxpayerSalesLedgerPerMachine)reportList.get(0)).getTaxpayerName());
/*  653 */     for (int il = 0; il < reportList.size() && reportList.size() > 0; il++) {
/*      */       
/*  655 */       paramMap.remove("MIN_FILTER");
/*  656 */       paramMap.put("MIN_FILTER", ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).getMin());
/*  657 */       List<TaxpayerSalesLedgerDetailsPerMachine> detailsList = this.reportsDao.getSalesLedgerSummaryMonthly(paramMap);
/*  658 */       if (null == detailsList || detailsList.size() == 0) {
/*      */         
/*  660 */         TaxpayerSalesLedgerDetailsPerMachine tmpValues = new TaxpayerSalesLedgerDetailsPerMachine();
/*      */         
/*  662 */         detailsList.add((null != tmpValues) ? tmpValues : new TaxpayerSalesLedgerDetailsPerMachine());
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/*  667 */       for (TaxpayerSalesLedgerDetailsPerMachine reportItem : detailsList) {
/*      */ 
/*      */         
/*  670 */         ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setAddValues(reportItem);
/*      */         
/*  672 */         switch (reportItem.getReportMonth()) {
/*      */           
/*      */           case 1:
/*  675 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setJANSalesReport(reportItem);
/*      */           
/*      */           case 2:
/*  678 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setFEBSalesReport(reportItem);
/*      */           
/*      */           case 3:
/*  681 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setMARSalesReport(reportItem);
/*      */           
/*      */           case 4:
/*  684 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setAPRSalesReport(reportItem);
/*      */           
/*      */           case 5:
/*  687 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setMAYSalesReport(reportItem);
/*      */           
/*      */           case 6:
/*  690 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setJUNSalesReport(reportItem);
/*      */           
/*      */           case 7:
/*  693 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setJULSalesReport(reportItem);
/*      */           
/*      */           case 8:
/*  696 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setAUGSalesReport(reportItem);
/*      */           
/*      */           case 9:
/*  699 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setSEPSalesReport(reportItem);
/*      */           
/*      */           case 10:
/*  702 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setOCTSalesReport(reportItem);
/*      */           
/*      */           case 11:
/*  705 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setNOVSalesReport(reportItem);
/*      */           
/*      */           case 12:
/*  708 */             ((TaxpayerSalesLedgerPerMachine)reportList.get(il)).setDECSalesReport(reportItem);
/*      */         } 
/*      */ 
/*      */       
/*      */       } 
/*      */     } 
/*  714 */     return this.reportsSalesLedgerGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getSalesLedgerSummaryCount(Map<String, Object> paramMap) {
/*  720 */     List<TaxpayerSalesLedgerPerMachine> reportList = new ArrayList<TaxpayerSalesLedgerPerMachine>();
/*      */ 
/*      */ 
/*      */     
/*  724 */     reportList = this.reportsDao.getSalesLedgerMachineList(paramMap);
/*      */     
/*  726 */     return reportList.size();
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsSalesLedgerGenerator(ReportGenerator reportsSalesLedgerGenerator) {
/*  731 */     this.reportsSalesLedgerGenerator = reportsSalesLedgerGenerator;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateListOfMachinesWithCancelledMinReport(Map<String, Object> paramMap) {
/*  737 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  739 */     List<MachinesWithCancelledMIN> reportList = new ArrayList<MachinesWithCancelledMIN>();
/*  740 */     reportList = this.reportsDao.getListOfMachinesWithCancelledMin(paramMap);
/*      */     
/*  742 */     return this.reportsMachinesWithCancelledMINGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateListOfMachineWithFinalPermit(Map<String, Object> paramMap) {
/*  748 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  750 */     List<MachineWithFinalPermit> reportList = new ArrayList<MachineWithFinalPermit>();
/*  751 */     paramMap.put("PERMIT_TYPE", "109");
/*  752 */     reportList = this.reportsDao.getListOfMachineWithFinalPermit(paramMap);
/*      */     
/*  754 */     return this.reportsListOfMachineWithFinalPermit.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateListOfMachinesWithProvisionalPermit(Map<String, Object> paramMap) {
/*  760 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  762 */     List<MachinesWithProvisionalPermit> reportList = new ArrayList<MachinesWithProvisionalPermit>();
/*      */     
/*  764 */     reportList = this.reportsDao.getListOfMachinesWithProvisionalPermit(paramMap);
/*      */     
/*  766 */     return this.reportsMachineWithProvisionalPermitGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateMachineRegistrationPerRDOGenerator(Map<String, Object> paramMap) {
/*  772 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  774 */     List<MachineRegistrationPerRDO> reportList = new ArrayList<MachineRegistrationPerRDO>();
/*      */     
/*  776 */     reportList = this.reportsDao.getMachineRegistrationPerRDO(paramMap);
/*      */     
/*  778 */     return this.reportsMachineRegistrationPerRDOGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateListOfRegisteredSpecialPurposeMachines(Map<String, Object> paramMap) {
/*  784 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  786 */     List<RegisteredSpecialPurposeMachines> reportList = new ArrayList<RegisteredSpecialPurposeMachines>();
/*      */     
/*  788 */     reportList = this.reportsDao.getListOfRegisteredSpecialPurposeMachines(paramMap);
/*      */     
/*  790 */     return this.reportsRegisteredSpecialPurposeMachinesGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateListOfRegisteredMachinesConvertedToFinalPermit(Map<String, Object> paramMap) {
/*  796 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  798 */     List<RegisteredMachinesConvertedToFinalPermit> reportList = new ArrayList<RegisteredMachinesConvertedToFinalPermit>();
/*      */     
/*  800 */     reportList = this.reportsDao.getListOfRegisteredMachinesConvertedToFinalPermit(paramMap);
/*      */     
/*  802 */     return this.reportsRegisteredMachinesConvertedToFinalGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateAccreditedSoftwarePerSupplierReport(Map<String, Object> paramMap) {
/*  808 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  810 */     List<AccreditedMachinesPerSupplierPerRDO> reportList = new ArrayList<AccreditedMachinesPerSupplierPerRDO>();
/*      */     
/*  812 */     reportList = this.reportsDao.getAccreditedSoftwarePerSupplier(paramMap);
/*      */     
/*  814 */     return this.reportsAccreditedSoftwarePerSupplierGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateUserAccessReport(Map<String, Object> paramMap) {
/*  821 */     String reportFormat = (String)paramMap.get("REPORT_FORMAT");
/*      */     
/*  823 */     List<UserAccessSearchReport> reportList = new ArrayList<UserAccessSearchReport>();
/*  824 */     reportList = this.reportsDao.getUserAccessList(paramMap);
/*      */     
/*  826 */     ReportContainer repCon = this.reportsUserAccessGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(reportList), getReportFormat(reportFormat));
/*      */ 
/*      */     
/*  829 */     return repCon;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportGenerator getReportsMachinesWithCancelledMINGenerator() {
/*  834 */     return this.reportsMachinesWithCancelledMINGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsMachinesWithCancelledMINGenerator(ReportGenerator reportsMachinesWithCancelledMINGenerator) {
/*  839 */     this.reportsMachinesWithCancelledMINGenerator = reportsMachinesWithCancelledMINGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsMachineWithProvisionalPermitGenerator(ReportGenerator reportsMachineWithProvisionalPermitGenerator) {
/*  844 */     this.reportsMachineWithProvisionalPermitGenerator = reportsMachineWithProvisionalPermitGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsMachineRegistrationPerRDOGenerator(ReportGenerator reportsMachineRegistrationPerRDOGenerator) {
/*  849 */     this.reportsMachineRegistrationPerRDOGenerator = reportsMachineRegistrationPerRDOGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsAccreditedSoftwarePerSupplierGenerator(ReportGenerator reportsAccreditedSoftwarePerSupplierGenerator) {
/*  854 */     this.reportsAccreditedSoftwarePerSupplierGenerator = reportsAccreditedSoftwarePerSupplierGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsAccreditedSoftwarePerSupplierFileName(String reportsAccreditedSoftwarePerSupplierFileName) {
/*  859 */     this.reportsAccreditedSoftwarePerSupplierFileName = reportsAccreditedSoftwarePerSupplierFileName;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsRegisteredSpecialPurposeMachinesGenerator(ReportGenerator reportsRegisteredSpecialPurposeMachinesGenerator) {
/*  864 */     this.reportsRegisteredSpecialPurposeMachinesGenerator = reportsRegisteredSpecialPurposeMachinesGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsRegisteredSpecialPurposeMachinesFileName(String reportsRegisteredSpecialPurposeMachinesFileName) {
/*  869 */     this.reportsRegisteredSpecialPurposeMachinesFileName = reportsRegisteredSpecialPurposeMachinesFileName;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsRegisteredMachinesConvertedToFinalGenerator(ReportGenerator reportsRegisteredMachinesConvertedToFinalGenerator) {
/*  874 */     this.reportsRegisteredMachinesConvertedToFinalGenerator = reportsRegisteredMachinesConvertedToFinalGenerator;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getMonthlySalesCount(Map<String, Object> paramMap) {
/*  880 */     ArrayList<TaxpayerWithMonthlySalesReportSubmission> reportList = (ArrayList<TaxpayerWithMonthlySalesReportSubmission>)this.reportsDao.getMonthlySalesList(paramMap);
/*  881 */     return reportList.size();
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int checkBusinessBranchFromITS(Map<String, Object> businessMap) {
/*  887 */     return this.businessDao.checkBusinessBranchFromITS(businessMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<ParameterizedObject> getReportListByProfile(Map<String, Object> paramMap) {
/*  893 */     return this.reportsDao.getReportListByProfile(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getAllMachineRegistrationCount(Map<String, Object> paramMap) {
/*  899 */     return this.reportsDao.getAllMachineRegistrationCount(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getAccreditedSoftwarePerSupplierCount(Map<String, Object> paramMap) {
/*  905 */     return this.reportsDao.getAccreditedSoftwarePerSupplierCount(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getMachineWithFinalPermitCount(Map<String, Object> paramMap) {
/*  911 */     paramMap.put("PERMIT_TYPE", "109");
/*  912 */     return this.reportsDao.getMachineWithFinalPermitCount(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getMachinesWithProvisionalPermitCount(Map<String, Object> paramMap) {
/*  918 */     return this.reportsDao.getMachinesWithProvisionalPermitCount(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getSPMPerRDOCount(Map<String, Object> paramMap) {
/*  924 */     return this.reportsDao.getSPMPerRDOCount(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getMachinesWithCancelledMINCount(Map<String, Object> paramMap) {
/*  930 */     return this.reportsDao.getMachinesWithCancelledMINCount(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int getMachinesConvertedToFinalPermitCount(Map<String, Object> paramMap) {
/*  936 */     return this.reportsDao.getMachinesConvertedToFinalPermitCount(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public void setManagementReportLocation(String managementReportLocation) {
/*  941 */     this.managementReportLocation = managementReportLocation;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<String> getComplianceReportPerTaxPayer(Map<String, Object> paramMap) {
/*  947 */     String fileExtension = ".pdf";
/*  948 */     List<String> filenameList = new ArrayList<String>();
/*      */ 
/*      */     
/*  951 */     SimpleDateFormat raw = new SimpleDateFormat("MM/dd/yyyy");
/*  952 */     SimpleDateFormat my = new SimpleDateFormat("MMyyyy");
/*      */     
/*  954 */     String rdo_code = paramMap.get("RDO_CODE").toString();
/*  955 */     String as_of_date = paramMap.get("AS_OF_DATE").toString();
/*  956 */     String reportType = paramMap.get("WordedReportType").toString();
/*      */ 
/*      */ 
/*      */     
/*      */     try {
/*  961 */       as_of_date = my.format(raw.parse(as_of_date));
/*      */     }
/*  963 */     catch (ParseException e) {
/*      */       
/*  965 */       this.log.error(e.getMessage(), e);
/*      */     } 
/*      */     
/*  968 */     File fileLocation = new File(this.managementReportLocation);
/*      */ 
/*      */     
/*  971 */     String fileName = "reports-" + reportType + "_" + as_of_date + "_" + rdo_code + fileExtension;
/*      */ 
/*      */ 
/*      */     
/*  975 */     for (File temp : fileLocation.listFiles()) {
/*      */       
/*  977 */       if (temp.getName().equalsIgnoreCase(fileName)) {
/*      */         
/*  979 */         filenameList.add(temp.getName());
/*  980 */         this.log.info("temp.getName(): " + temp.getName());
/*      */       } 
/*      */     } 
/*      */     
/*  984 */     return filenameList;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<String> getComplianceReportPerRDO(Map<String, Object> paramMap) {
/*  990 */     String fileExtension = ".pdf";
/*  991 */     List<String> filenameList = new ArrayList<String>();
/*      */ 
/*      */     
/*  994 */     SimpleDateFormat raw = new SimpleDateFormat("MM/dd/yyyy");
/*  995 */     SimpleDateFormat my = new SimpleDateFormat("MMyyyy");
/*      */     
/*  997 */     String rdo_code = paramMap.get("RDO_CODE").toString();
/*  998 */     String as_of_date = paramMap.get("AS_OF_DATE").toString();
/*  999 */     String reportType = paramMap.get("WordedReportType").toString();
/*      */ 
/*      */ 
/*      */     
/*      */     try {
/* 1004 */       as_of_date = my.format(raw.parse(as_of_date));
/*      */     }
/* 1006 */     catch (ParseException e) {
/*      */       
/* 1008 */       this.log.error(e.getMessage(), e);
/*      */     } 
/*      */     
/* 1011 */     File fileLocation = new File(this.managementReportLocation);
/*      */ 
/*      */     
/* 1014 */     String fileName = "reports-" + reportType + "_" + as_of_date + "_" + rdo_code + fileExtension;
/*      */ 
/*      */ 
/*      */     
/* 1018 */     this.log.info("fileName: " + fileName);
/* 1019 */     for (File temp : fileLocation.listFiles()) {
/*      */       
/* 1021 */       if (temp.getName().equalsIgnoreCase(fileName)) {
/*      */         
/* 1023 */         filenameList.add(temp.getName());
/* 1024 */         this.log.info("temp.getName(): " + temp.getName());
/*      */       } 
/*      */     } 
/*      */     
/* 1028 */     return filenameList;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<String> getMatching(Map<String, Object> paramMap) {
/* 1034 */     String fileExtension = ".pdf";
/* 1035 */     List<String> filenameList = new ArrayList<String>();
/*      */ 
/*      */     
/* 1038 */     SimpleDateFormat raw = new SimpleDateFormat("MM/dd/yyyy");
/* 1039 */     SimpleDateFormat my = new SimpleDateFormat("MMyyyy");
/*      */     
/* 1041 */     String rdo_code = paramMap.get("RDO_CODE").toString();
/* 1042 */     String as_of_date = paramMap.get("AS_OF_DATE").toString();
/* 1043 */     String reportType = paramMap.get("WordedReportType").toString();
/*      */ 
/*      */ 
/*      */     
/*      */     try {
/* 1048 */       as_of_date = my.format(raw.parse(as_of_date));
/*      */     }
/* 1050 */     catch (ParseException e) {
/*      */       
/* 1052 */       this.log.error(e.getMessage(), e);
/*      */     } 
/*      */     
/* 1055 */     File fileLocation = new File(this.managementReportLocation);
/*      */ 
/*      */     
/* 1058 */     String fileName = "reports-" + reportType + "_" + as_of_date + "_" + rdo_code + fileExtension;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1063 */     this.log.info("fileName: " + fileName + " : fileLocation.list().length : " + (fileLocation.list()).length);
/*      */     
/* 1065 */     for (File temp : fileLocation.listFiles()) {
/*      */       
/* 1067 */       if (temp.getName().equalsIgnoreCase(fileName)) {
/*      */         
/* 1069 */         filenameList.add(temp.getName());
/* 1070 */         this.log.info("temp.getName(): " + temp.getName());
/*      */       } 
/*      */     } 
/*      */     
/* 1074 */     return filenameList;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateComplianceReportPerTaxPayer(Map<String, Object> paramMap) {
/* 1080 */     String fileExtension = ".pdf";
/* 1081 */     File finalFile = null;
/*      */     
/* 1083 */     SimpleDateFormat raw = new SimpleDateFormat("MM/dd/yyyy");
/* 1084 */     SimpleDateFormat my = new SimpleDateFormat("MMyyyy");
/*      */     
/* 1086 */     String rdo_code = paramMap.get("RDO_CODE").toString();
/* 1087 */     String as_of_date = paramMap.get("AS_OF_DATE").toString();
/*      */ 
/*      */     
/* 1090 */     String reportType = paramMap.get("WordedReportType").toString();
/*      */ 
/*      */     
/*      */     try {
/* 1094 */       as_of_date = my.format(raw.parse(as_of_date));
/*      */     }
/* 1096 */     catch (ParseException e) {
/*      */       
/* 1098 */       this.log.error(e.getMessage(), e);
/*      */     } 
/*      */     
/* 1101 */     File fileLocation = new File(this.managementReportLocation);
/*      */ 
/*      */     
/* 1104 */     String fileName = "reports-" + reportType + "_" + as_of_date + "_" + rdo_code + fileExtension;
/*      */ 
/*      */ 
/*      */     
/* 1108 */     this.log.info("fileName: " + fileName);
/* 1109 */     for (File temp : fileLocation.listFiles()) {
/*      */       
/* 1111 */       if (temp.getName().equalsIgnoreCase(fileName)) {
/*      */         
/* 1113 */         finalFile = temp;
/* 1114 */         this.log.info("finalFile.getName(): " + finalFile.getName());
/*      */       } 
/*      */     } 
/*      */     
/* 1118 */     ReportContainer reportContainer = null;
/* 1119 */     FileInputStream fis = null;
/*      */     
/*      */     try {
/* 1122 */       fis = new FileInputStream(finalFile);
/*      */       
/*      */       try {
/* 1125 */         byte[] dataHolder = IOUtils.toByteArray(fis);
/* 1126 */         reportContainer = new ReportContainer(dataHolder, finalFile.getName(), ReportFormat.PDF);
/*      */       }
/* 1128 */       catch (IOException e) {
/*      */         
/* 1130 */         this.log.error(e.getMessage(), e);
/*      */       }
/*      */     
/* 1133 */     } catch (FileNotFoundException e) {
/*      */       
/* 1135 */       this.log.error(e.getMessage(), e);
/*      */     }
/*      */     finally {
/*      */       
/* 1139 */       if (fis != null) {
/*      */         
/*      */         try {
/*      */           
/* 1143 */           fis.close();
/*      */         }
/* 1145 */         catch (IOException e) {
/*      */           
/* 1147 */           this.log.error(e.getMessage(), e);
/*      */         } 
/*      */       }
/*      */     } 
/*      */     
/* 1152 */     return reportContainer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateComplianceReportPerRDO(Map<String, Object> paramMap) {
/* 1158 */     String fileExtension = ".pdf";
/* 1159 */     File finalFile = null;
/*      */     
/* 1161 */     SimpleDateFormat raw = new SimpleDateFormat("MM/dd/yyyy");
/* 1162 */     SimpleDateFormat my = new SimpleDateFormat("MMyyyy");
/*      */     
/* 1164 */     String rdo_code = paramMap.get("RDO_CODE").toString();
/* 1165 */     String as_of_date = paramMap.get("AS_OF_DATE").toString();
/*      */ 
/*      */     
/* 1168 */     String reportType = paramMap.get("WordedReportType").toString();
/*      */ 
/*      */     
/*      */     try {
/* 1172 */       as_of_date = my.format(raw.parse(as_of_date));
/*      */     }
/* 1174 */     catch (ParseException e) {
/*      */       
/* 1176 */       this.log.error(e.getMessage(), e);
/*      */     } 
/*      */     
/* 1179 */     File fileLocation = new File(this.managementReportLocation);
/*      */ 
/*      */     
/* 1182 */     String fileName = "reports-" + reportType + "_" + as_of_date + "_" + rdo_code + fileExtension;
/*      */ 
/*      */ 
/*      */     
/* 1186 */     this.log.info("fileName: " + fileName);
/* 1187 */     for (File temp : fileLocation.listFiles()) {
/*      */       
/* 1189 */       if (temp.getName().equalsIgnoreCase(fileName)) {
/*      */         
/* 1191 */         finalFile = temp;
/* 1192 */         this.log.info("finalFile.getName(): " + finalFile.getName());
/*      */       } 
/*      */     } 
/*      */     
/* 1196 */     ReportContainer reportContainer = null;
/* 1197 */     FileInputStream fis = null;
/*      */     
/*      */     try {
/* 1200 */       fis = new FileInputStream(finalFile);
/*      */       
/*      */       try {
/* 1203 */         byte[] dataHolder = IOUtils.toByteArray(fis);
/* 1204 */         reportContainer = new ReportContainer(dataHolder, finalFile.getName(), ReportFormat.PDF);
/*      */       }
/* 1206 */       catch (IOException e) {
/*      */         
/* 1208 */         this.log.error(e.getMessage(), e);
/*      */       }
/*      */     
/* 1211 */     } catch (FileNotFoundException e) {
/*      */       
/* 1213 */       this.log.error(e.getMessage(), e);
/*      */     }
/*      */     finally {
/*      */       
/* 1217 */       if (fis != null) {
/*      */         
/*      */         try {
/*      */           
/* 1221 */           fis.close();
/*      */         }
/* 1223 */         catch (IOException e) {
/*      */           
/* 1225 */           this.log.error(e.getMessage(), e);
/*      */         } 
/*      */       }
/*      */     } 
/*      */     
/* 1230 */     return reportContainer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateMatching(Map<String, Object> paramMap) {
/* 1236 */     String fileExtension = ".pdf";
/* 1237 */     File finalFile = null;
/*      */     
/* 1239 */     SimpleDateFormat raw = new SimpleDateFormat("MM/dd/yyyy");
/* 1240 */     SimpleDateFormat my = new SimpleDateFormat("MMyyyy");
/*      */     
/* 1242 */     String rdo_code = paramMap.get("RDO_CODE").toString();
/* 1243 */     String as_of_date = paramMap.get("AS_OF_DATE").toString();
/*      */ 
/*      */     
/* 1246 */     String reportType = paramMap.get("WordedReportType").toString();
/*      */ 
/*      */     
/*      */     try {
/* 1250 */       as_of_date = my.format(raw.parse(as_of_date));
/*      */     }
/* 1252 */     catch (ParseException e) {
/*      */       
/* 1254 */       this.log.error(e.getMessage(), e);
/*      */     } 
/*      */     
/* 1257 */     File fileLocation = new File(this.managementReportLocation);
/*      */ 
/*      */     
/* 1260 */     String fileName = "reports-" + reportType + "_" + as_of_date + "_" + rdo_code + fileExtension;
/*      */ 
/*      */ 
/*      */     
/* 1264 */     this.log.info("fileName: " + fileName);
/* 1265 */     for (File temp : fileLocation.listFiles()) {
/*      */       
/* 1267 */       if (temp.getName().equalsIgnoreCase(fileName)) {
/*      */         
/* 1269 */         finalFile = temp;
/* 1270 */         this.log.info("finalFile.getName(): " + finalFile.getName());
/*      */       } 
/*      */     } 
/*      */     
/* 1274 */     ReportContainer reportContainer = null;
/* 1275 */     FileInputStream fis = null;
/*      */     
/* 1277 */     this.log.info("finalFile: " + finalFile);
/*      */ 
/*      */     
/*      */     try {
/* 1281 */       fis = new FileInputStream(finalFile);
/*      */       
/*      */       try {
/* 1284 */         byte[] dataHolder = IOUtils.toByteArray(fis);
/* 1285 */         reportContainer = new ReportContainer(dataHolder, finalFile.getName(), ReportFormat.PDF);
/*      */       }
/* 1287 */       catch (IOException e) {
/*      */         
/* 1289 */         this.log.error(e.getMessage(), e);
/*      */       }
/*      */     
/* 1292 */     } catch (FileNotFoundException e) {
/*      */       
/* 1294 */       this.log.error(e.getMessage(), e);
/*      */     }
/*      */     finally {
/*      */       
/* 1298 */       if (fis != null) {
/*      */         
/*      */         try {
/*      */           
/* 1302 */           fis.close();
/*      */         }
/* 1304 */         catch (IOException e) {
/*      */           
/* 1306 */           this.log.error(e.getMessage(), e);
/*      */         } 
/*      */       }
/*      */     } 
/*      */     
/* 1311 */     return reportContainer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<ParameterizedObject> getBIRRegionAccessByUserNameAndLoginType(Map<String, Object> param) {
/* 1317 */     return this.businessDao.getBIRRegionAccessByUserNameAndLoginType(param);
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReportsUserAccessGenerator(ReportGenerator reportsUserAccessGenerator) {
/* 1322 */     this.reportsUserAccessGenerator = reportsUserAccessGenerator;
/*      */   }
/*      */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\reports\ReportsServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */