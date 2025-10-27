/*     */ package com.indracompany.sysad.main;
/*     */ 
/*     */ import com.indracompany.acrsal.model.sales.ReportedSalesHist;
/*     */ import com.indracompany.sysad.api.AuditTrailService;
/*     */ import com.indracompany.sysad.dao.AuditTrailDao;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class AuditTrailServiceImpl
/*     */   implements AuditTrailService
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*  20 */   private final Logger log = Logger.getLogger(AuditTrailServiceImpl.class);
/*     */   
/*     */   private AuditTrailDao auditTrailDao;
/*     */   
/*     */   public AuditTrailDao getAuditTrailDao() {
/*  25 */     return this.auditTrailDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setAuditTrailDao(AuditTrailDao auditTrailDao) {
/*  30 */     this.auditTrailDao = auditTrailDao;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public Object getAuditTrailList(Map<String, Object> paramMap) {
/*  36 */     Object resultsList = new ArrayList();
/*     */     
/*  38 */     if (checkIfValidAuditParam(paramMap)) {
/*     */       
/*  40 */       if (paramMap.get("OBJECT_TYPE").equals("167"))
/*     */       {
/*  42 */         resultsList = this.auditTrailDao.getUserHist(paramMap);
/*     */       }
/*  44 */       else if (paramMap.get("OBJECT_TYPE").equals("161"))
/*     */       {
/*  46 */         resultsList = this.auditTrailDao.getRDOAccessesHist(paramMap);
/*     */       }
/*  48 */       else if (paramMap.get("OBJECT_TYPE").equals("164"))
/*     */       {
/*  50 */         resultsList = this.auditTrailDao.getRoleAccessesHist(paramMap);
/*     */       }
/*  52 */       else if (paramMap.get("OBJECT_TYPE").equals("165"))
/*     */       {
/*  54 */         resultsList = this.auditTrailDao.getSalesReportHist(paramMap);
/*     */       }
/*  56 */       else if (paramMap.get("OBJECT_TYPE").equals("150"))
/*     */       {
/*  58 */         resultsList = this.auditTrailDao.getMachineHist(paramMap);
/*     */       }
/*  60 */       else if (paramMap.get("OBJECT_TYPE").equals("149"))
/*     */       {
/*  62 */         resultsList = this.auditTrailDao.getMachineRelHist(paramMap);
/*     */       }
/*  64 */       else if (paramMap.get("OBJECT_TYPE").equals("160"))
/*     */       {
/*  66 */         resultsList = this.auditTrailDao.getPermitRegAppHist(paramMap);
/*     */       }
/*  68 */       else if (paramMap.get("OBJECT_TYPE").equals("147"))
/*     */       {
/*  70 */         resultsList = this.auditTrailDao.getAccountEnrollmentHist(paramMap);
/*     */       }
/*  72 */       else if (paramMap.get("OBJECT_TYPE").equals("163"))
/*     */       {
/*  74 */         resultsList = this.auditTrailDao.getReportsAccessesHist(paramMap);
/*     */       }
/*  76 */       else if (paramMap.get("OBJECT_TYPE").equals("166"))
/*     */       {
/*  78 */         resultsList = this.auditTrailDao.getUserAccessesHist(paramMap);
/*     */       }
/*  80 */       else if (paramMap.get("OBJECT_TYPE").equals("168"))
/*     */       {
/*  82 */         resultsList = this.auditTrailDao.getPrintingHist(paramMap);
/*     */       }
/*  84 */       else if (paramMap.get("OBJECT_TYPE").equals("148"))
/*     */       {
/*  86 */         resultsList = this.auditTrailDao.getAccreditationAppHist(paramMap);
/*     */       }
/*  88 */       else if (paramMap.get("OBJECT_TYPE").equals("162"))
/*     */       {
/*  90 */         resultsList = this.auditTrailDao.getReferencesHist(paramMap);
/*     */       }
/*     */     
/*     */     } else {
/*     */       
/*  95 */       this.log.info("##Invalid Combination of Search Parameters for Audit Trail.");
/*     */     } 
/*     */     
/*  98 */     return resultsList;
/*     */   }
/*     */ 
/*     */   
/*     */   public List<ReportedSalesHist> getAuditTrailSalesReport(Map<String, Object> paramMap) {
/* 103 */     List<ReportedSalesHist> resultsList = null;
/*     */     
/* 105 */     if (checkIfValidAuditParam(paramMap)) {
/*     */       
/* 107 */       resultsList = this.auditTrailDao.getSalesReportHist(paramMap);
/*     */     }
/*     */     else {
/*     */       
/* 111 */       this.log.info("##Invalid Combination of Search Parameters for Audit Trail.");
/*     */     } 
/* 113 */     return resultsList;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean checkIfValidAuditParam(Map<String, Object> paramMap) {
/* 118 */     String systemType = (String)paramMap.get("SYSTEM_TYPE");
/* 119 */     String objectType = (String)paramMap.get("OBJECT_TYPE");
/*     */     
/* 121 */     List<String> accregSalesValidList = new ArrayList<String>(Arrays.asList(new String[] { "147", "161", "163", "167" }));
/*     */ 
/*     */     
/* 124 */     if (systemType.equals("002")) {
/*     */       
/* 126 */       List<String> accregValidList = new ArrayList<String>(Arrays.asList(new String[] { "148", "168", "149", "150", "160" }));
/*     */ 
/*     */ 
/*     */       
/* 130 */       return (accregValidList.contains(objectType) || accregSalesValidList.contains(objectType));
/*     */     } 
/* 132 */     if (systemType.equals("003")) {
/*     */       
/* 134 */       List<String> salesValidList = new ArrayList<String>(Arrays.asList(new String[] { "165" }));
/*     */       
/* 136 */       return (salesValidList.contains(objectType) || accregSalesValidList.contains(objectType));
/*     */     } 
/* 138 */     if (systemType.equals("001")) {
/*     */       
/* 140 */       List<String> internalUsersValidList = new ArrayList<String>(Arrays.asList(new String[] { "162", "164", "166" }));
/*     */ 
/*     */       
/* 143 */       return internalUsersValidList.contains(objectType);
/*     */     } 
/*     */     
/* 146 */     return false;
/*     */   }
/*     */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\sysad\main\AuditTrailServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */