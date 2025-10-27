/*    */ package com.indracompany.acrsal.main.enrollment;
/*    */ 
/*    */ import com.indracompany.acrsal.api.enrollment.TaskManagerService;
/*    */ import com.indracompany.acrsal.dao.business.BusinessDao;
/*    */ import com.indracompany.acrsal.dao.util.UtilDao;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TaskManagerServiceImpl
/*    */   implements TaskManagerService
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */   private BusinessDao businessDao;
/*    */   private UtilDao utilDao;
/*    */   
/*    */   public List<Map<String, Object>> getStatusCountFromBusiness(String loginType) {
/* 21 */     List<Map<String, Object>> listOfStatusCount = this.businessDao.getStatusCountFromBusiness(loginType);
/*    */     
/* 23 */     return listOfStatusCount;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public List<Map<String, Object>> getAccrediationStatusCount(Map<String, Object> param) {
/* 29 */     return this.utilDao.getAccrediationStatusCount(param);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public List<Map<String, Object>> getEnrollmentStatusCount(Map<String, Object> param) {
/* 35 */     return this.utilDao.getEnrollmentStatusCount(param);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public List<Map<String, Object>> getRegistrationStatusCount(Map<String, Object> param) {
/* 41 */     return this.utilDao.getRegistrationStatusCount(param);
/*    */   }
/*    */ 
/*    */   
/*    */   public void setBusinessDao(BusinessDao businessDao) {
/* 46 */     this.businessDao = businessDao;
/*    */   }
/*    */ 
/*    */   
/*    */   public void setUtilDao(UtilDao utilDao) {
/* 51 */     this.utilDao = utilDao;
/*    */   }
/*    */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\enrollment\TaskManagerServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */