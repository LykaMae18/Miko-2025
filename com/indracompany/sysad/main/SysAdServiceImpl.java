/*     */ package com.indracompany.sysad.main;
/*     */ 
/*     */ import com.indracompany.acrsal.model.sysad.MenuFunction;
/*     */ import com.indracompany.acrsal.models.ParameterizedObject;
/*     */ import com.indracompany.sysad.api.SysAdService;
/*     */ import com.indracompany.sysad.dao.rdo.RdoAccessesDao;
/*     */ import com.indracompany.sysad.dao.rdo.RdoAccessesHistDao;
/*     */ import com.indracompany.sysad.dao.reports.ReportsAccessDao;
/*     */ import com.indracompany.sysad.dao.reports.ReportsAccessHistDao;
/*     */ import com.indracompany.sysad.dao.role.RoleAccessDao;
/*     */ import com.indracompany.sysad.forms.RDOAccessInquiryForm;
/*     */ import com.indracompany.sysad.forms.RoleAccessInquiryForm;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Calendar;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SysAdServiceImpl
/*     */   implements SysAdService
/*     */ {
/*     */   private static final long serialVersionUID = -3042073258817739677L;
/*  30 */   private static final Logger log = Logger.getLogger(SysAdServiceImpl.class);
/*     */   
/*     */   private RdoAccessesDao rdoAccessesDao;
/*     */   
/*     */   private RoleAccessDao roleAccessDao;
/*     */   
/*     */   private ReportsAccessDao reportsAccessDao;
/*     */   private ReportsAccessHistDao reportsAccessHistDao;
/*     */   private RdoAccessesHistDao rdoAccessHistDao;
/*     */   
/*     */   public List<ParameterizedObject> getRoleList() {
/*  41 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getOfficeListforRoleAccess() {
/*  47 */     return this.roleAccessDao.getOfficeListforRoleAccess();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getDivisionListforRoleAccess(String officeCd) {
/*  53 */     return this.roleAccessDao.getDivisionListforRoleAccess(officeCd);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getRoleListforRoleAccess(String divisionCd, String systemType) {
/*  59 */     return this.roleAccessDao.getRoleListforRoleAccess(divisionCd, systemType);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getRdoList() {
/*  65 */     return this.rdoAccessesDao.getRdoList();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getCurrentRdoList(Map<String, Object> params) {
/*  71 */     return this.rdoAccessesDao.getCurrentRdoList(params);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void addRdoCodes(String username, Map<String, Object> params) {
/*  78 */     HashMap<String, Object> paramList = new HashMap<String, Object>();
/*  79 */     paramList.put("USER_NAME", params.get("USER_NAME"));
/*  80 */     paramList.put("LOGIN_TYPE", params.get("LOGIN_TYPE"));
/*     */     
/*  82 */     List<ParameterizedObject> previous = this.rdoAccessesDao.getCurrentRdoList(params);
/*  83 */     for (int i = 0; i < previous.size(); i++) {
/*     */       
/*  85 */       paramList = new HashMap<String, Object>();
/*  86 */       paramList.put("USER_NAME", params.get("USER_NAME"));
/*  87 */       paramList.put("LOGIN_TYPE", params.get("LOGIN_TYPE"));
/*  88 */       paramList.put("RDO_CODE", ((ParameterizedObject)previous.get(i)).getCode());
/*  89 */       paramList.put("AUDIT_TRAIL", params.get("AUDIT_TRAIL"));
/*  90 */       paramList.put("TRANS_TYPE", "042");
/*  91 */       this.rdoAccessHistDao.insertRecordRdoAccessesHist(paramList);
/*     */     } 
/*  93 */     this.rdoAccessesDao.deleteRdoCodes(params);
/*     */     
/*  95 */     List<String> rdoCodes = (List<String>)params.get("RDO_CODE");
/*  96 */     for (int j = 0; j < rdoCodes.size(); j++) {
/*     */       
/*  98 */       paramList = new HashMap<String, Object>();
/*  99 */       paramList.put("USER_NAME", params.get("USER_NAME"));
/* 100 */       paramList.put("LOGIN_TYPE", params.get("LOGIN_TYPE"));
/* 101 */       paramList.put("RDO_CODE", ((String)rdoCodes.get(j)).toString().trim());
/* 102 */       paramList.put("AUDIT_TRAIL", params.get("AUDIT_TRAIL"));
/* 103 */       this.rdoAccessesDao.insertRdoCodes(paramList);
/*     */       
/* 105 */       paramList.put("TRANS_TYPE", "040");
/* 106 */       this.rdoAccessHistDao.insertRecordRdoAccessesHist(paramList);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<MenuFunction> getRoleMatrixListforRoleAccess(Map<String, Object> params) {
/* 113 */     return this.roleAccessDao.getRoleMatrixListforRoleAccess(params);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<MenuFunction> getMenuFunctionList(String roleCode) {
/* 119 */     return this.roleAccessDao.getMenuFunctionList(roleCode);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void deleteRoleList(Map<String, Object> params) {
/* 125 */     this.roleAccessDao.deleteRoleList(params);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void insertRoleAccess(Map<String, Object> params) {
/* 131 */     this.roleAccessDao.insertRoleAccess(params);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void deleteUserAssignments(Map<String, Object> params) {
/* 137 */     this.roleAccessDao.deleteUserAssignments(params);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void insertUserAssignments(Map<String, Object> params) {
/* 143 */     this.roleAccessDao.insertUserAssignments(params);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public int checkIfUserAssignmentExist(Map<String, Object> params) {
/* 149 */     return this.roleAccessDao.checkIfUserAssignmentExist(params);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getUserLoginFromRole(String userRole) {
/* 155 */     return this.roleAccessDao.getUserLoginFromRole(userRole);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<RoleAccessInquiryForm> getRoleAccessInquiryList(Map<String, Object> searchParam) {
/* 161 */     return this.roleAccessDao.getRoleAccessInquiryList(searchParam);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getReportTypes(String loginType) {
/* 167 */     return this.reportsAccessDao.getReportTypes(loginType);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getUserReportTypes(Map<String, Object> params) {
/* 173 */     return this.reportsAccessDao.getUserReportTypes(params);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void addReportsAccess(Map<String, Object> params) {
/* 180 */     HashMap<String, Object> paramList = new HashMap<String, Object>();
/* 181 */     paramList.put("USER_NAME", params.get("USER_NAME"));
/* 182 */     paramList.put("LOGIN_TYPE", params.get("LOGIN_TYPE"));
/*     */     
/* 184 */     List<ParameterizedObject> previous = this.reportsAccessDao.getUserReportTypes(paramList);
/* 185 */     for (int i = 0; i < previous.size(); i++) {
/*     */       
/* 187 */       paramList = new HashMap<String, Object>();
/* 188 */       paramList.put("USER_NAME", params.get("USER_NAME"));
/* 189 */       paramList.put("LOGIN_TYPE", params.get("LOGIN_TYPE"));
/* 190 */       paramList.put("REPORT_CODE", ((ParameterizedObject)previous.get(i)).getCode());
/* 191 */       paramList.put("AUDIT_TRAIL", params.get("AUDIT_TRAIL"));
/* 192 */       paramList.put("TRANS_TYPE", "042");
/* 193 */       this.reportsAccessHistDao.insertRecordReportsAccessHist(paramList);
/*     */     } 
/*     */     
/* 196 */     this.reportsAccessDao.deleteUserReportsAccess(paramList);
/*     */     
/* 198 */     List<String> reportCodes = (List<String>)params.get("REPORT_CODE");
/* 199 */     for (int j = 0; j < reportCodes.size(); j++) {
/*     */       
/* 201 */       paramList = new HashMap<String, Object>();
/* 202 */       paramList.put("USER_NAME", params.get("USER_NAME"));
/* 203 */       paramList.put("LOGIN_TYPE", params.get("LOGIN_TYPE"));
/* 204 */       paramList.put("REPORT_CODE", ((String)reportCodes.get(j)).toString().trim());
/* 205 */       paramList.put("AUDIT_TRAIL", params.get("AUDIT_TRAIL"));
/* 206 */       this.reportsAccessDao.addUserReportsAccess(paramList);
/*     */       
/* 208 */       paramList.put("TRANS_TYPE", "040");
/* 209 */       this.reportsAccessHistDao.insertRecordReportsAccessHist(paramList);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void setRdoAccessesDao(RdoAccessesDao rdoAccessesDao) {
/* 215 */     this.rdoAccessesDao = rdoAccessesDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setRoleAccessDao(RoleAccessDao roleAccessDao) {
/* 220 */     this.roleAccessDao = roleAccessDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setReportsAccessDao(ReportsAccessDao reportsAccessDao) {
/* 225 */     this.reportsAccessDao = reportsAccessDao;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<RDOAccessInquiryForm> getRdoAccessesInquiry(Map<String, Object> inqMap) {
/* 232 */     RDOAccessInquiryForm inquiryForm = (RDOAccessInquiryForm)inqMap.get("RAIF");
/*     */     
/* 234 */     SimpleDateFormat formatter = new SimpleDateFormat("MMMMM yyyy");
/* 235 */     SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
/*     */     
/* 237 */     String dateGiven = inquiryForm.getDateCreate();
/* 238 */     String strFromDate = "";
/* 239 */     String strToDate = "";
/* 240 */     String strToDateFinal = "";
/*     */ 
/*     */     
/*     */     try {
/* 244 */       strFromDate = sdf.format(formatter.parse(dateGiven));
/* 245 */       strToDate = sdf.format(formatter.parse(dateGiven));
/*     */       
/* 247 */       String[] strDate = strToDate.split("/");
/* 248 */       Calendar cal = Calendar.getInstance();
/* 249 */       Calendar calLastDay = Calendar.getInstance();
/*     */       
/* 251 */       cal.set(Integer.parseInt(strDate[2]), Integer.parseInt(strDate[0]) - 1, 1);
/* 252 */       cal.getActualMaximum(5);
/* 253 */       calLastDay.set(Integer.parseInt(strDate[2]), Integer.parseInt(strDate[0]) - 1, cal.getActualMaximum(5));
/*     */       
/* 255 */       strToDateFinal = sdf.format(calLastDay.getTime());
/*     */     }
/* 257 */     catch (ParseException e) {
/*     */       
/* 259 */       log.debug("Error exceptions");
/*     */     } 
/*     */     
/* 262 */     inqMap.put("APP_FROM_DATE", strFromDate);
/* 263 */     inqMap.put("APP_TO_DATE", strToDateFinal);
/*     */     
/* 265 */     return this.reportsAccessDao.getRdoAccessesInquiry(inqMap);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void setReportsAccessHistDao(ReportsAccessHistDao reportsAccessHistDao) {
/* 271 */     this.reportsAccessHistDao = reportsAccessHistDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setRdoAccessHistDao(RdoAccessesHistDao rdoAccessHistDao) {
/* 276 */     this.rdoAccessHistDao = rdoAccessHistDao;
/*     */   }
/*     */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\sysad\main\SysAdServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */